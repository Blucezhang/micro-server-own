"""Additional write-path checks for the disposable marketplace acceptance stack."""

from decimal import Decimal, InvalidOperation


def money(value, context, failure):
    try:
        return Decimal(str(value))
    except (InvalidOperation, TypeError):
        raise failure(context + " did not contain a valid amount")


def verify_product_edit(api, actors, product_id, suffix, require_field, failure):
    path = "/product/api/v1/merchant/products/" + str(product_id)
    command = {
        "partyId": actors["merchant_id"],
        "name": "Updated acceptance product " + suffix,
        "skuCode": "E2E-" + suffix.upper(),
        "originalPrice": "19.90",
        "promotionPrice": "18.90",
        "content": "Synthetic data for local end-to-end acceptance only",
        "specification": "one unit",
        "priceChangeReason": "Disposable price-edit verification",
    }
    key = "e2e-product-edit-" + suffix
    updated = api.data("PUT", path, actors["merchant_token"], command, key=key)
    replay = api.data("PUT", path, actors["merchant_token"], command, key=key)
    if updated.get("id") != product_id or replay.get("id") != product_id:
        raise failure("product edit or its same-key replay returned another product")
    changed = dict(command, promotionPrice="17.90")
    api.request("PUT", path, actors["merchant_token"], changed, expected=(409,),
                headers={"Idempotency-Key": key})
    public = api.data("GET", "/product/api/v1/products/" + str(product_id))
    if public.get("name") != command["name"] or money(public.get("promotionPrice"), "public product", failure) != Decimal("18.90"):
        raise failure("product edit was not visible in the public catalogue")
    audits = api.data("GET", path + "/price-audits", actors["merchant_token"])
    items = require_field(audits, "items", "product price audits")
    if len(items) != 1:
        raise failure("same-key product edit created an unexpected number of price audits")
    print("PASS: product edit, visible price, price audit, and same-key replay/conflict")
    return {"path": path, "body": command, "key": key}


def verify_stock_adjustment(api, actors, product_id, suffix, failure):
    path = "/inventory/api/v1/stocks/" + str(product_id)
    command = {"merchantId": actors["merchant_id"], "adjustmentType": "INBOUND",
               "quantity": 2, "reason": "Disposable local acceptance adjustment"}
    key = "e2e-stock-adjust-" + suffix
    api.data("POST", path + "/adjustments", actors["merchant_token"], command, key=key)
    api.data("POST", path + "/adjustments", actors["merchant_token"], command, key=key)
    stock = api.data("GET", path + "?merchantId=" + str(actors["merchant_id"]), actors["merchant_token"])
    if (stock.get("availableQuantity"), stock.get("reservedQuantity"), stock.get("soldQuantity")) != (7, 0, 0):
        raise failure("stock adjustment replay changed inventory more than once")
    print("PASS: merchant stock adjustment and same-key replay")


def verify_freight_rule(api, actors, suffix, failure):
    path = "/order/api/v1/merchant/freight-rule"
    command = {"fixedAmount": "2.00", "freeThreshold": "50.00"}
    api.data("PUT", path, actors["merchant_token"], command, key="e2e-freight-" + suffix)
    rule = api.data("GET", path, actors["merchant_token"])
    if money(rule.get("fixedAmount"), "freight rule", failure) != Decimal("2.00"):
        raise failure("merchant freight rule was not persisted")
    print("PASS: merchant freight rule write and readback")


def verify_catalog_operations(api, actors, product_id, suffix, require_field, failure):
    edit = verify_product_edit(api, actors, product_id, suffix, require_field, failure)
    verify_stock_adjustment(api, actors, product_id, suffix, failure)
    verify_freight_rule(api, actors, suffix, failure)
    return edit
