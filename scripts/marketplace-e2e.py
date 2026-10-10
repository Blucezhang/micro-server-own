#!/usr/bin/env python3
"""Run a synthetic buyer-to-after-sale flow against an isolated local stack."""

import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid
from decimal import Decimal, InvalidOperation


class AcceptanceFailure(RuntimeError):
    pass


class MarketplaceApi:
    def __init__(self, base_url):
        self.base_url = base_url.rstrip("/")

    def request(self, method, path, token=None, body=None, expected=(200,), headers=None):
        request_headers = {"Accept": "application/json"}
        if token:
            request_headers["Authorization"] = "Bearer " + token
        if body is not None:
            request_headers["Content-Type"] = "application/json"
        if headers:
            request_headers.update(headers)
        request_body = None if body is None else json.dumps(body).encode("utf-8")
        request = urllib.request.Request(
            self.base_url + path, data=request_body, headers=request_headers, method=method
        )
        try:
            with urllib.request.urlopen(request, timeout=15) as response:
                status = response.status
                raw = response.read()
        except urllib.error.HTTPError as error:
            status = error.code
            raw = error.read()
        except (urllib.error.URLError, TimeoutError) as error:
            raise AcceptanceFailure("request failed for " + method + " " + path + ": " + str(error))
        try:
            payload = json.loads(raw.decode("utf-8")) if raw else {}
        except (UnicodeDecodeError, json.JSONDecodeError):
            payload = {"message": "response was not JSON"}
        if status not in expected:
            message = payload.get("message", "") if isinstance(payload, dict) else ""
            raise ApiResponseFailure(status, method, path, message)
        return payload

    def data(self, method, path, token=None, body=None, expected=(200,), key=None, headers=None):
        request_headers = dict(headers or {})
        if key:
            request_headers["Idempotency-Key"] = key
        payload = self.request(method, path, token, body, expected, request_headers)
        if not isinstance(payload, dict) or "data" not in payload:
            raise AcceptanceFailure("response envelope missing data for " + method + " " + path)
        return payload["data"]

    def wait_for_data(self, path, token, predicate, timeout=120):
        deadline = time.monotonic() + timeout
        last_status = "not reached"
        while time.monotonic() < deadline:
            try:
                value = self.data("GET", path, token=token)
                last_status = str(value.get("status")) if isinstance(value, dict) else "unexpected response"
                if predicate(value):
                    return value
            except ApiResponseFailure as error:
                if error.status not in (404, 502, 503, 504):
                    raise
                last_status = str(error.status)
            time.sleep(2)
        raise AcceptanceFailure("timed out waiting for " + path + " (last state: " + last_status + ")")


class ApiResponseFailure(AcceptanceFailure):
    def __init__(self, status, method, path, message):
        self.status = status
        detail = " (" + message + ")" if message else ""
        super().__init__(method + " " + path + " returned HTTP " + str(status) + detail)


def require_field(value, field, context):
    if not isinstance(value, dict) or value.get(field) is None:
        raise AcceptanceFailure(context + " response is missing " + field)
    return value[field]


def registration_command(login_name, password, label):
    phone_suffix = "".join(str(int(character, 16) % 10) for character in login_name[-10:])
    return {
        "loginName": login_name,
        "password": password,
        "name": "E2E " + label,
        "email": login_name + "@example.test",
        "phone": "1" + phone_suffix,
    }


def register(api, login_name, password, label):
    return api.data(
        "POST",
        "/user/api/v1/auth/registrations",
        body=registration_command(login_name, password, label),
        expected=(200,),
        key="e2e-register-" + login_name,
    )


def login(api, login_name, password, actor_type):
    return api.data(
        "POST",
        "/user/login/token",
        body={"loginUserName": login_name, "password": password, "actorType": actor_type},
        expected=(200,),
    )


def wait_for_auth_service(api, suffix, timeout=120):
    deadline = time.monotonic() + timeout
    path = "/user/login/token"
    body = {"loginUserName": "e2e-readiness-" + suffix, "password": "invalid", "actorType": "BUYER"}
    while time.monotonic() < deadline:
        try:
            api.request("POST", path, body=body, expected=(403,))
            print("Ready: user-party")
            return
        except ApiResponseFailure as error:
            if error.status == 403:
                print("Ready: user-party")
                return
            if error.status not in (404, 502, 503, 504):
                raise
        time.sleep(2)
    raise AcceptanceFailure("timed out waiting for user-party registration")


def provision_local_system_role(login_name):
    project = os.environ["E2E_COMPOSE_PROJECT"]
    env_file = os.environ["E2E_COMPOSE_ENV_FILE"]
    compose_file = os.environ["E2E_COMPOSE_FILE"]
    password = os.environ["NEO4J_PASSWORD"]
    compose = [
        "docker", "compose", "--project-name", project,
        "--env-file", env_file, "-f", compose_file,
        "exec", "-T", "neo4j", "cypher-shell", "--username", "neo4j", "--password", password,
    ]
    query = (
        "MATCH (u:LoginUser {loginName: '" + login_name + "'}) "
        "MERGE (r:Role {name: 'ROLE_SYSTEM'}) "
        "SET r.note = 'Disposable local acceptance fixture' "
        "MERGE (r)-[:belong]->(u) RETURN id(u)\n"
    )
    result = subprocess.run(compose, input=query, text=True, capture_output=True, check=False)
    if result.returncode:
        raise AcceptanceFailure("could not grant the SYSTEM role in the disposable Neo4j fixture")


def write(api, method, path, token, body=None, expected=(200,), label="write", headers=None):
    return api.data(
        method, path, token=token, body=body, expected=expected,
        key="e2e-" + label + "-" + uuid.uuid4().hex, headers=headers,
    )


def create_actors(api, suffix, password):
    system_name = "e2e-system-" + suffix
    register(api, system_name, password, "System")
    provision_local_system_role(system_name)
    system = login(api, system_name, password, "SYSTEM")
    system_token = require_field(system, "accessToken", "system login")
    buyer_name = "e2e-buyer-" + suffix
    register(api, buyer_name, password, "Buyer")
    buyer = login(api, buyer_name, password, "BUYER")
    buyer_token = require_field(buyer, "accessToken", "buyer login")
    merchant_name = "e2e-merchant-" + suffix
    merchant_user = register(api, merchant_name, password, "Merchant")
    merchant_login_id = require_field(merchant_user, "loginUserId", "merchant registration")
    write(
        api, "PUT", "/user/api/v1/system/accounts/" + str(merchant_login_id) + "/merchant-role",
        system_token, expected=(200,), label="grant-merchant",
    )
    merchant = login(api, merchant_name, password, "MERCHANT")
    merchant_token = require_field(merchant, "accessToken", "merchant login")
    merchant_id = require_field(merchant, "actorId", "merchant login")
    other_name = "e2e-other-" + suffix
    register(api, other_name, password, "Other Buyer")
    other = login(api, other_name, password, "BUYER")
    return {
        "system_token": system_token,
        "buyer_token": buyer_token,
        "merchant_token": merchant_token,
        "merchant_id": merchant_id,
        "other_buyer_token": require_field(other, "accessToken", "second buyer login"),
    }


def verify_registration_idempotency(api, suffix, password):
    login_name = "e2e-idem-" + suffix
    key = "e2e-register-" + login_name
    created = register(api, login_name, password, "Idempotency")
    replayed = register(api, login_name, password, "Idempotency")
    if require_field(created, "loginUserId", "first registration") != require_field(
            replayed, "loginUserId", "registration replay"):
        raise AcceptanceFailure("same-key registration replay did not return the original account")
    api.request(
        "POST", "/user/api/v1/auth/registrations",
        body=registration_command(login_name, password, "Changed Payload"), expected=(409,),
        headers={"Idempotency-Key": key},
    )
    print("PASS: same-key registration replay and changed-payload conflict")


def create_product(api, actors, suffix):
    product = write(
        api, "POST", "/product/api/v1/merchant/products", actors["merchant_token"],
        {
            "partyId": actors["merchant_id"],
            "name": "Disposable acceptance product " + suffix,
            "skuCode": "E2E-" + suffix.upper(),
            "originalPrice": "19.90",
            "promotionPrice": "19.90",
            "content": "Synthetic data for local end-to-end acceptance only",
            "specification": "one unit",
        },
        expected=(200,), label="create-product",
    )
    product_id = require_field(product, "id", "product creation")
    write(
        api, "PUT", "/inventory/api/v1/stocks/" + str(product_id), actors["system_token"],
        {"merchantId": actors["merchant_id"], "availableQuantity": 5},
        expected=(200,), label="seed-stock",
    )
    print("PASS: merchant product and isolated stock fixture")
    return product_id


def create_coupon(api, actors):
    now = int(time.time() * 1000)
    template = write(
        api, "POST", "/sale/api/v1/coupons/merchant/templates", actors["merchant_token"],
        {
            "name": "Disposable acceptance coupon", "totalQuantity": 3,
            "minimumAmount": "10.00", "discountAmount": "3.00",
            "claimStartsAt": now - 10_000, "claimEndsAt": now + 300_000,
            "expiresAt": now + 86_400_000,
        }, label="create-coupon-template",
    )
    template_id = require_field(template, "id", "coupon template")
    coupon = write(
        api, "POST", "/sale/api/v1/coupons/claim", actors["buyer_token"],
        {"couponType": "STORE", "merchantId": actors["merchant_id"], "merchantTemplateId": template_id},
        label="claim-coupon",
    )
    coupon_no = require_field(coupon, "couponNo", "claimed coupon")
    api.request(
        "POST", "/sale/api/v1/coupons/claim", actors["buyer_token"],
        {"couponType": "STORE", "merchantId": actors["merchant_id"], "merchantTemplateId": template_id},
        expected=(409,), headers={"Idempotency-Key": "e2e-duplicate-coupon-claim"},
    )
    print("PASS: merchant coupon template, buyer claim, and one-per-buyer rule")
    return coupon_no


def assert_coupon_status(api, buyer_token, coupon_no, expected_status):
    coupons = api.data("GET", "/sale/api/v1/coupons/me", buyer_token)
    coupon = next((item for item in coupons if item.get("couponNo") == coupon_no), None)
    if coupon is None or coupon.get("status") != expected_status:
        actual = "missing" if coupon is None else str(coupon.get("status"))
        raise AcceptanceFailure("coupon state is " + actual + ", expected " + expected_status)


def place_order(api, actors, product_id):
    address = write(
        api, "POST", "/user/api/v1/addresses", actors["buyer_token"],
        {
            "recipientName": "E2E Buyer", "mobile": "15550000001",
            "province": "Test Province", "city": "Test City", "district": "Test District",
            "detail": "Disposable local acceptance address", "defaultAddress": True,
        },
        expected=(200,), label="create-address",
    )
    address_id = require_field(address, "id", "address creation")
    cart = write(
        api, "POST", "/order/api/v1/cart/items", actors["buyer_token"],
        {"productId": product_id, "quantity": 1}, expected=(200,), label="add-cart-item",
    )
    cart_id = require_field(cart, "id", "cart creation")
    coupon_no = create_coupon(api, actors)
    checkout = {
        "cartItemIds": [cart_id], "addressId": address_id,
        "coupons": [{
            "couponNo": coupon_no, "merchantId": actors["merchant_id"],
            "applicableAmount": "19.90",
        }],
    }
    quote = write(api, "POST", "/order/api/v1/checkouts/quote", actors["buyer_token"], checkout, label="checkout-quote")
    try:
        payable = Decimal(str(require_field(quote, "payableAmount", "checkout quote")))
        discount = Decimal(str(require_field(quote, "discountAmount", "checkout quote")))
    except InvalidOperation:
        raise AcceptanceFailure("checkout quote has an invalid payable amount")
    if payable != Decimal("16.90") or discount != Decimal("3.00"):
        raise AcceptanceFailure("checkout quote did not apply the expected 3.00 coupon discount")
    order = write(api, "POST", "/order/api/v1/orders", actors["buyer_token"], checkout, expected=(200,), label="create-order")
    order_no = require_field(order, "orderNo", "order creation")
    api.wait_for_data(
        "/order/api/v1/orders/" + order_no, actors["buyer_token"],
        lambda value: isinstance(value, dict) and value.get("status") == "PENDING_PAYMENT",
    )
    assert_coupon_status(api, actors["buyer_token"], coupon_no, "RESERVED")
    print("PASS: buyer address, cart, discounted quote, coupon reservation, and Saga order")
    return order_no, coupon_no


def complete_payment(api, actors, order_no, coupon_no):
    payment = write(
        api, "POST", "/settlement/api/v1/payments", actors["buyer_token"],
        {"orderNo": order_no, "channel": "MOCK_WECHAT"}, expected=(200,), label="create-payment",
    )
    payment_no = require_field(payment, "paymentNo", "payment creation")
    callback_token = os.environ["PAYMENT_MOCK_CALLBACK_TOKEN"]
    callback_path = "/settlement/api/v1/payments/mock-callbacks/MOCK_WECHAT"
    callback_headers = {"X-Mock-Payment-Token": callback_token}
    callback_body = {
        "paymentNo": payment_no,
        "providerPaymentNo": require_field(payment, "providerPaymentNo", "payment creation"),
        "amount": require_field(payment, "amount", "payment creation"),
        "result": "SUCCESS",
    }
    invalid_callback = dict(callback_body)
    invalid_callback["amount"] = format(Decimal(str(callback_body["amount"])) - Decimal("0.01"), "f")
    api.request("POST", callback_path, body=invalid_callback, expected=(409,), headers=callback_headers)
    before_payment = api.data("GET", "/settlement/api/v1/payments/" + payment_no, actors["buyer_token"])
    if before_payment.get("status") != "CREATED":
        raise AcceptanceFailure("rejected payment callback changed the payment state")
    callback = api.data("POST", callback_path, body=callback_body, expected=(200,), headers=callback_headers)
    if require_field(callback, "status", "mock payment callback") != "SUCCEEDED":
        raise AcceptanceFailure("mock payment callback did not reach SUCCEEDED")
    replay = api.data("POST", callback_path, body=callback_body, expected=(200,), headers=callback_headers)
    if require_field(replay, "status", "replayed payment callback") != "SUCCEEDED":
        raise AcceptanceFailure("duplicate payment callback did not remain SUCCEEDED")
    api.wait_for_data(
        "/order/api/v1/orders/" + order_no, actors["buyer_token"],
        lambda value: isinstance(value, dict) and value.get("status") == "PAID",
    )
    assert_coupon_status(api, actors["buyer_token"], coupon_no, "USED")
    detail = api.data("GET", "/order/api/v1/orders/" + order_no + "/detail", actors["buyer_token"])
    sub_orders = require_field(detail, "subOrders", "order detail")
    items = require_field(detail, "items", "order detail")
    if not sub_orders or not items:
        raise AcceptanceFailure("paid order did not contain a sub-order and order item")
    sub_order_no = require_field(sub_orders[0], "subOrderNo", "order detail sub-order")
    order_item_id = require_field(items[0], "id", "order detail item")
    print("PASS: callback amount rejection, success replay, and paid-order transition")
    return sub_order_no, order_item_id


def complete_return(api, actors, suffix, product_id, coupon_no, sub_order_no, order_item_id):
    write(
        api, "POST", "/order/api/v1/sub-orders/" + sub_order_no + "/ship", actors["merchant_token"],
        {"logisticsCompany": "E2E Local Carrier", "trackingNo": "E2E-" + suffix.upper()},
        label="ship-order",
    )
    after_sale = write(
        api, "POST", "/order/api/v1/after-sales", actors["buyer_token"],
        {
            "subOrderNo": sub_order_no, "type": "RETURN_AND_REFUND",
            "reason": "Synthetic end-to-end return", "items": [{"orderItemId": order_item_id, "quantity": 1}],
        },
        expected=(200,), label="create-after-sale",
    )
    after_sale_no = require_field(after_sale, "afterSaleNo", "after-sale creation")
    write(
        api, "POST", "/order/api/v1/after-sales/" + after_sale_no + "/audit", actors["merchant_token"],
        {"approved": True, "remark": "Approved by local acceptance fixture"}, label="approve-after-sale",
    )
    write(
        api, "POST", "/order/api/v1/after-sales/" + after_sale_no + "/return-shipment", actors["buyer_token"],
        {"logisticsCompany": "E2E Local Carrier", "trackingNo": "RET-" + suffix.upper()},
        label="return-shipment",
    )
    write(
        api, "POST", "/order/api/v1/after-sales/" + after_sale_no + "/receive-return", actors["merchant_token"],
        label="receive-return",
    )
    api.wait_for_data(
        "/order/api/v1/after-sales/" + after_sale_no, actors["buyer_token"],
        lambda value: isinstance(value, dict) and value.get("status") == "REFUNDED",
    )
    stock = api.data(
        "GET", "/inventory/api/v1/stocks/" + str(product_id)
        + "?merchantId=" + str(actors["merchant_id"]), actors["merchant_token"],
    )
    if (stock.get("availableQuantity") != 5 or stock.get("reservedQuantity") != 0
            or stock.get("soldQuantity") != 0):
        raise AcceptanceFailure("return refund did not restore the isolated stock balances")
    assert_coupon_status(api, actors["buyer_token"], coupon_no, "USED")
    print("PASS: shipment, return approval, return receipt, refund, and stock compensation")
    return after_sale_no


def verify_access_boundaries(api, actors, order_no):
    other_token = actors["other_buyer_token"]
    api.request("GET", "/order/api/v1/orders/" + order_no + "/detail", other_token, expected=(403, 404))
    api.request("GET", "/product/api/v1/merchant/products", actors["buyer_token"], expected=(403,))
    api.request("GET", "/order/api/v1/orders", expected=(401,))
    print("PASS: buyer ownership, role isolation, and anonymous access rejection")


def run():
    base_url = os.environ.get("GATEWAY_URL", "http://127.0.0.1:9632").rstrip("/")
    suffix = uuid.uuid4().hex[:10]
    password = "E2e-" + uuid.uuid4().hex + "-Aa9!"
    api = MarketplaceApi(base_url)
    api.wait_for_data("/product/api/v1/products", None, lambda value: isinstance(value, dict) and isinstance(value.get("items"), list))
    print("PASS: gateway and product service are reachable")
    wait_for_auth_service(api, suffix)
    verify_registration_idempotency(api, suffix, password)
    actors = create_actors(api, suffix, password)
    product_id = create_product(api, actors, suffix)
    order_no, coupon_no = place_order(api, actors, product_id)
    sub_order_no, order_item_id = complete_payment(api, actors, order_no, coupon_no)
    complete_return(api, actors, suffix, product_id, coupon_no, sub_order_no, order_item_id)
    verify_access_boundaries(api, actors, order_no)
    print("Local acceptance passed; all created accounts and business rows are disposable.")


if __name__ == "__main__":
    try:
        run()
    except (AcceptanceFailure, KeyError) as error:
        print("Local acceptance failed: " + str(error), file=sys.stderr)
        sys.exit(1)
