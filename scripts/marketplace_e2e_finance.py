"""Exchange and simulated settlement checks against disposable Compose data only."""

import os
import re
import subprocess
from decimal import Decimal, InvalidOperation
from pathlib import Path


def prepare_high_value_product(api, actors, product_id, suffix, write, failure):
    product = write(
        api, "PUT", "/product/api/v1/merchant/products/" + str(product_id), actors["merchant_token"],
        {"partyId": actors["merchant_id"], "name": "High-value acceptance product " + suffix,
         "skuCode": "E2E-" + suffix.upper(), "originalPrice": "200.00", "promotionPrice": "200.00",
         "content": "Synthetic local acceptance data", "specification": "one unit",
         "priceChangeReason": "Disposable exchange and settlement verification"},
        label="high-value-product-edit",
    )
    if Decimal(str(product.get("promotionPrice"))) != Decimal("200.00"):
        raise failure("high-value product price edit did not persist")
    print("PASS: high-value product edit for synthetic settlement")


def complete_exchange(api, actors, suffix, product_id, sub_order_no, order_item_id,
                      write, require_field, failure):
    write(api, "POST", "/order/api/v1/sub-orders/" + sub_order_no + "/ship",
          actors["merchant_token"],
          {"logisticsCompany": "E2E Local Carrier", "trackingNo": "EXC-" + suffix.upper()},
          label="ship-exchange-order")
    sale = write(api, "POST", "/order/api/v1/after-sales", actors["buyer_token"],
                 {"subOrderNo": sub_order_no, "type": "EXCHANGE", "reason": "Synthetic same-SKU exchange",
                  "items": [{"orderItemId": order_item_id, "quantity": 1}]},
                 label="create-exchange")
    number = require_field(sale, "afterSaleNo", "exchange creation")
    path = "/order/api/v1/after-sales/" + number
    write(api, "POST", path + "/audit", actors["merchant_token"],
          {"approved": True, "remark": "Disposable exchange approved"}, label="approve-exchange")
    write(api, "POST", path + "/return-shipment", actors["buyer_token"],
          {"logisticsCompany": "E2E Local Carrier", "trackingNo": "EXR-" + suffix.upper()},
          label="exchange-return-shipment")
    received = write(api, "POST", path + "/receive-return", actors["merchant_token"],
                     label="receive-exchange-return")
    if received.get("status") != "EXCHANGE_PENDING_SHIPMENT":
        raise failure("exchange did not reach pending replacement shipment")
    shipped = write(api, "POST", path + "/exchange-shipment", actors["merchant_token"],
                    {"logisticsCompany": "E2E Local Carrier", "trackingNo": "EXS-" + suffix.upper()},
                    label="ship-replacement")
    if shipped.get("status") != "EXCHANGE_SHIPPED":
        raise failure("exchange replacement did not reach shipped status")
    write(api, "POST", path + "/exchange-receive", actors["buyer_token"],
          label="confirm-replacement")
    detail = api.data("GET", path, actors["buyer_token"])
    if detail.get("status") != "EXCHANGED":
        raise failure("buyer confirmation did not complete exchange")
    stock = api.data("GET", "/inventory/api/v1/stocks/" + str(product_id)
                     + "?merchantId=" + str(actors["merchant_id"]), actors["merchant_token"])
    if (stock.get("availableQuantity"), stock.get("reservedQuantity"), stock.get("soldQuantity")) != (6, 0, 1):
        raise failure("same-SKU exchange changed stock balances unexpectedly")
    print("PASS: same-SKU exchange, replacement receipt, and inventory readback")
    return number


def disposable_mysql(api, sql, failure):
    project = os.environ.get("E2E_COMPOSE_PROJECT", "")
    env_file = os.environ.get("E2E_COMPOSE_ENV_FILE", "")
    compose_file = os.environ.get("E2E_COMPOSE_FILE", "")
    expected_compose = Path(__file__).resolve().parents[1] / "docker-compose.yml"
    if not re.fullmatch(r"micro-server-own-e2e-[0-9a-f]{12}", project):
        raise failure("MySQL fixture access requires a disposable E2E Compose project")
    if not Path(env_file).is_file() or Path(compose_file).resolve() != expected_compose:
        raise failure("MySQL fixture access requires the local acceptance Compose files")
    command = ["docker", "compose", "--project-name", project, "--env-file", env_file,
               "-f", compose_file, "exec", "-T", "mysql", "sh", "-c",
               'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -umicro micro --batch --skip-column-names -e "$1"',
               "--", sql]
    result = subprocess.run(command, text=True, capture_output=True, check=False)
    if result.returncode:
        raise failure("disposable MySQL fixture query failed: " + result.stderr.strip()[:300])
    api.evidence.append({"kind": "mysql", "query": sql, "result": result.stdout.strip()})
    return result.stdout.strip()


def verify_uncertain_idempotency(api, actors, edit, product_id, failure):
    key = edit["key"]
    if not re.fullmatch(r"e2e-product-edit-[0-9a-f]{10}", key):
        raise failure("unsafe synthetic idempotency key for MySQL fixture")
    merchant_id = actors["merchant_id"]
    if not isinstance(merchant_id, int) or merchant_id <= 0:
        raise failure("invalid synthetic merchant id for idempotency fixture")
    where = "idempotency_key='" + key + "' AND actor_id=" + str(merchant_id)
    before = disposable_mysql(api, "SELECT COUNT(*), MIN(status) FROM trade_idempotency WHERE " + where, failure)
    if before != "1\tCOMPLETED":
        raise failure("expected one completed synthetic product edit idempotency record")
    # Simulate an expired but unresolved response record; never release or mark it successful.
    disposable_mysql(api, "UPDATE trade_idempotency SET status='PROCESSING', "
                     "expires_at=DATE_SUB(NOW(), INTERVAL 1 DAY) WHERE " + where, failure)
    api.request("PUT", edit["path"], actors["merchant_token"], edit["body"],
                expected=(409,), headers={"Idempotency-Key": key})
    after = disposable_mysql(api, "SELECT COUNT(*), MIN(status) FROM trade_idempotency WHERE " + where, failure)
    if after != "1\tPROCESSING":
        raise failure("expired unresolved idempotency record was unlocked or changed")
    public = api.data("GET", "/product/api/v1/products/" + str(product_id))
    if Decimal(str(public.get("promotionPrice"))) != Decimal("200.00"):
        raise failure("uncertain-key replay changed the current product price")
    print("PASS: expired unresolved PROCESSING key remains blocked without replay")


def verify_database_state(api, actors, product_id, first, second, failure):
    values = [first["order"], first["coupon"], first["after_sale"],
              second["order"], second["coupon"], second["after_sale"]]
    if any(not re.fullmatch(r"[A-Za-z0-9-]+", value) for value in values):
        raise failure("unsafe synthetic identifier for final MySQL readback")
    merchant_id = actors["merchant_id"]
    if not isinstance(merchant_id, int) or merchant_id <= 0 or not isinstance(product_id, int):
        raise failure("invalid synthetic product or merchant id for final MySQL readback")
    stock = disposable_mysql(api, "SELECT available_quantity, reserved_quantity, sold_quantity FROM inv_stock "
                             "WHERE product_id=" + str(product_id) + " AND merchant_id=" + str(merchant_id), failure)
    if stock != "6\t0\t1":
        raise failure("final inventory database state does not match the exchange result")
    adjustment = disposable_mysql(api, "SELECT COUNT(*) FROM inventory_adjustment WHERE product_id="
                                  + str(product_id) + " AND merchant_id=" + str(merchant_id), failure)
    if adjustment != "1":
        raise failure("replayed stock adjustment produced an unexpected number of database rows")
    for flow, expected_sale in ((first, "REFUNDED"), (second, "EXCHANGED")):
        coupon = disposable_mysql(api, "SELECT status FROM mkt_user_coupon WHERE coupon_no='"
                                  + flow["coupon"] + "'", failure)
        sale = disposable_mysql(api, "SELECT status FROM ord_after_sale WHERE after_sale_no='"
                                + flow["after_sale"] + "'", failure)
        order = disposable_mysql(api, "SELECT COUNT(*) FROM ord_order WHERE order_no='"
                                 + flow["order"] + "'", failure)
        payment = disposable_mysql(api, "SELECT status FROM pay_payment WHERE order_no='"
                                   + flow["order"] + "'", failure)
        if (coupon, sale, order, payment) != ("USED", expected_sale, "1", "SUCCEEDED"):
            raise failure("coupon, after-sale, order, or payment database state differs from API readback")
    print("PASS: isolated MySQL stock, adjustment, coupon, order, payment, and after-sale readback")


def settle_and_withdraw(api, actors, suffix, order_no, write, require_field, failure):
    if not re.fullmatch(r"[A-Za-z0-9-]+", order_no):
        raise failure("unsafe synthetic order number for MySQL fixture")
    merchant_id = actors["merchant_id"]
    if not isinstance(merchant_id, int) or merchant_id <= 0:
        raise failure("invalid synthetic merchant id for MySQL fixture")
    where = "order_no='" + order_no + "' AND merchant_id=" + str(merchant_id)
    before = disposable_mysql(api, "SELECT status FROM stl_merchant_receivable WHERE " + where, failure)
    if before != "PENDING":
        raise failure("paid synthetic order has no unique pending merchant receivable")
    # Only this disposable order is aged; the business settlement period is unchanged.
    disposable_mysql(api, "UPDATE stl_merchant_receivable SET created_at=DATE_SUB(NOW(), INTERVAL 8 DAY) WHERE " + where, failure)
    internal = "/settlement/api/v1/merchant/settlement/internal"
    batch = write(api, "POST", internal + "/batches/run", actors["system_token"],
                  label="run-settlement-batch")
    require_field(batch, "batchNo", "settlement batch")
    settled = disposable_mysql(api, "SELECT status FROM stl_merchant_receivable WHERE " + where, failure)
    if settled != "SETTLED":
        raise failure("settlement batch did not settle the synthetic receivable")
    base = "/settlement/api/v1/merchant/settlement"
    balance = api.data("GET", base + "/balance", actors["merchant_token"])
    try:
        available = Decimal(str(require_field(balance, "availableAmount", "merchant balance")))
    except InvalidOperation:
        raise failure("merchant balance is not numeric")
    if available < Decimal("100.00"):
        raise failure("settled synthetic order did not meet the withdrawal minimum")
    withdrawal = write(api, "POST", base + "/withdrawals", actors["merchant_token"],
                       {"amount": "100.00"}, label="request-withdrawal")
    number = require_field(withdrawal, "withdrawalNo", "withdrawal request")
    if withdrawal.get("status") != "REQUESTED":
        raise failure("withdrawal request did not reserve the amount")
    after = api.data("GET", base + "/balance", actors["merchant_token"])
    if Decimal(str(require_field(after, "availableAmount", "merchant balance"))) != available - Decimal("100.00"):
        raise failure("withdrawal did not reduce available merchant balance")
    if not re.fullmatch(r"[A-Za-z0-9-]+", number):
        raise failure("unsafe synthetic withdrawal number for MySQL fixture")
    paid = write(api, "POST", internal + "/withdrawals/" + number + "/simulate-paid",
                 actors["system_token"], label="simulate-withdrawal-paid")
    if paid.get("status") != "PAID":
        raise failure("simulated withdrawal did not reach PAID")
    status = disposable_mysql(api, "SELECT status FROM stl_withdrawal WHERE withdrawal_no='" + number + "'", failure)
    if status != "PAID":
        raise failure("simulated withdrawal PAID status was not persisted")
    final_balance = api.data("GET", base + "/balance", actors["merchant_token"])
    if Decimal(str(require_field(final_balance, "availableAmount", "merchant balance"))) != available - Decimal("100.00"):
        raise failure("simulated payout incorrectly restored the merchant balance")
    print("PASS: synthetic receivable, settlement, withdrawal reservation and simulated paid readback")
