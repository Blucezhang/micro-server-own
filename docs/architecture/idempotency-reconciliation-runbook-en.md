# Read-only Runbook for Idempotency `PROCESSING` Records

> Status: read-only triage only. This runbook does not authorize repair or unlocking. Use it when an operation's outcome is uncertain and retries with the same key keep returning `409`.

## Safety boundaries

- A record is scoped by `service_name + actor_id + actor_type + request_path + idempotency_key`; its request fingerprint is SHA-256 over serialized arguments.
- A `COMPLETED` record replays its cached `Resp` during retention. After expiry, a later request may delete it and execute again.
- A `PROCESSING` record is not automatically deleted even after `expires_at`; same-key requests continue to return `409`. Expiry makes it a triage candidate, not proof that the original request stopped or had no effect.
- If controller execution throws, the idempotency record is deleted. If saving the completed response fails, the business operation may already have committed while the record remains `PROCESSING`. Cross-service work has no global transaction or exactly-once guarantee.
- There is no system API or supported SQL procedure to complete, delete, or unlock an uncertain record. Do not modify `trade_idempotency` directly.

## Read-only triage

1. Collect the service/operation, actor ID and type, request path, idempotency key, event time, and correlation ID from the caller. Do not request passwords, access tokens, or payment credentials; do not paste the raw key into shell commands, tickets, or shared logs.
2. Pause automated retries for this command. Do not resend with a new key: that bypasses the guard and may duplicate side effects. Record the time and response for the `409` retry.
3. In an approved read-only database session, use a prepared statement with bound parameters to inspect the exact scope. Use the path recorded by the service; gateway prefixes may have been stripped. Read a summary only; do not select `response_body`.

```sql
SELECT id,
       service_name,
       actor_id,
       actor_type,
       request_path,
       SHA2(idempotency_key, 256) AS key_fingerprint,
       request_hash,
       status,
       response_status,
       response_body IS NOT NULL AS has_cached_response,
       created_at,
       expires_at,
       completed_at
FROM trade_idempotency
WHERE service_name = ?
  AND actor_id = ?
  AND actor_type = ?
  AND request_path = ?
  AND idempotency_key = ?;
```

Do not expose the raw `idempotency_key` or `response_body` in query results, screenshots, or tickets. If the database client automatically expands all columns, use an explicit field list.
`response_status` stores the cached `Resp.status` value; it is not the HTTP transport status.

4. Confirm no application instance is still processing the request. Review the owning service's logs by correlation ID, actor, and time window. Missing business details in logs are not evidence that the operation did not run.
5. Ask the owning domain team to perform read-only checks. For example, order creation requires checking the order/Saga, sub-orders/items, and stock or coupon reservations; payment requires checking the payment, order state, and settlement records. Graph and MySQL writes must be checked separately. Follow each service's data ownership and access approval; an idempotency row alone cannot prove the outcome in other stores.
6. Record evidence and conclusion. If the result is complete and unique, leave the record unchanged and have the business owner communicate the outcome through an existing read-only business view. If partial effects exist or the result cannot be proven, leave it `PROCESSING` and escalate to the service owner. Do not replay with a new key or delete the row to restore availability.

## Prohibited actions

- `DELETE`, `UPDATE`, manual status/expiry/fingerprint/business-key edits.
- Running a generic cleanup against `PROCESSING` rows or retrying solely because `expires_at` has passed.
- Copying credentials from logs or inspecting `response_body` for the original request. The table stores the response, not the request body.

## Prerequisites for automated recovery

Automation requires a defined operator authority, audit fields, in-flight request detection, domain-specific side-effect verifiers, partial-commit handling, approval requirements, concurrency locks, rollback strategy, and fault-injection tests against real dependencies. Setting `PROCESSING` to `COMPLETED` generically or deleting it to allow a retry is unsafe. Those policies are not yet approved; this runbook authorizes no data repair.
