# Sprint 3 Event Contracts

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

## 1) Contract Rules
- Event names are versioned (`<domain>.<action>.v1`).
- Envelope fields are mandatory for all events.
- `eventId` is globally unique UUID and used for idempotency.
- `traceId` and `requestId` are required for correlation.
- Producers must never include secrets (passwords, payment tokens, full card data).

## 2) Common Envelope

```json
{
  "eventId": "uuid",
  "eventType": "string",
  "eventVersion": 1,
  "occurredAt": "2026-03-26T14:05:00Z",
  "source": "SERVICE-NAME",
  "traceId": "a1b2c3",
  "requestId": "f2aa7a2b-7d0e-4fd3-a617-b4f3271a8a9a",
  "payload": {}
}
```

## 3) payment.authorized.v1

**Producer:** `PAYMENT-SERVICE`  
**Consumers:** `ORDER-SERVICE`, `NOTIFICATION-SERVICE` (optional)

Payload:
```json
{
  "paymentId": "a1e2c3d4-1111-2222-3333-444455556666",
  "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
  "userId": "a6c5a0de-2b0f-4a1d-9c5d-1f02e0a5e9e2",
  "amount": 16997.00,
  "currency": "INR",
  "providerRef": "prov_12345",
  "authorizedAt": "2026-03-26T14:05:00Z"
}
```

Validation notes:
- `amount > 0`
- `currency` ISO-4217 3-char code
- `orderId`, `paymentId`, `userId` are UUID

## 4) payment.failed.v1

**Producer:** `PAYMENT-SERVICE`  
**Consumers:** `ORDER-SERVICE`

Payload:
```json
{
  "paymentId": "a1e2c3d4-1111-2222-3333-444455556666",
  "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
  "userId": "a6c5a0de-2b0f-4a1d-9c5d-1f02e0a5e9e2",
  "amount": 16997.00,
  "currency": "INR",
  "failureCode": "DECLINED",
  "failureReason": "Insufficient funds",
  "failedAt": "2026-03-26T14:05:10Z",
  "retryable": false
}
```

Validation notes:
- `failureCode` enum example: `DECLINED`, `TIMEOUT`, `PROVIDER_ERROR`
- Consumer must trigger compensation idempotently by `eventId`

## 5) order.finalized.v1

**Producer:** `ORDER-SERVICE`  
**Consumers:** `NOTIFICATION-SERVICE`, analytics/reporting (future)

Payload:
```json
{
  "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
  "userId": "a6c5a0de-2b0f-4a1d-9c5d-1f02e0a5e9e2",
  "finalStatus": "CONFIRMED",
  "paymentStatus": "AUTHORIZED",
  "totalAmount": 16997.00,
  "currency": "INR",
  "finalizedAt": "2026-03-26T14:06:00Z"
}
```

Validation notes:
- `finalStatus` enum: `CONFIRMED`, `CANCELLED`
- Notification consumer must enforce exactly-once-effect via processed-events table

## 6) Broker Routing (recommended)
- Exchange: `eshop.events`
- Routing keys:
  - `payment.authorized.v1`
  - `payment.failed.v1`
  - `order.finalized.v1`
- Queue examples:
  - `order.payment.events.q`
  - `notification.order.events.q`

## 7) Idempotency and Compatibility
- Consumer idempotency key: `eventId` + `consumerName`.
- Producers must keep backward compatibility within same version.
- Breaking changes require new event version (`.v2`).

## 8) Error Handling and Retries
- Transient consume failures: retry with backoff.
- Permanent failures: dead-letter queue + operator alert.
- Include `traceId` in all retry and DLQ logs.

