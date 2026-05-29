# PocketBase Web API Coverage

This document maps `pocketbase-kmp` modules to the official PocketBase Web APIs.

Official docs used:
- https://pocketbase.io/docs/api-records/
- https://pocketbase.io/docs/api-collections/
- https://pocketbase.io/docs/api-files/
- https://pocketbase.io/docs/api-realtime/

## Records API (`pocketbase-records`)

Implemented:
- `GET /api/collections/{collection}/records`
  - supports `page`, `perPage`, `filter`, `sort`, `expand`, `fields`, `skipTotal`
- `GET /api/collections/{collection}/records/{id}`
  - supports `expand`, `fields`
- `POST /api/collections/{collection}/records`
  - supports optional response selectors via query: `expand`, `fields`
- `PATCH /api/collections/{collection}/records/{id}`
  - supports optional response selectors via query: `expand`, `fields`
- `PUT /api/collections/{collection}/records`
- `DELETE /api/collections/{collection}/records/{id}`

Also implemented:
- `POST /api/batch` via `BatchService`

## Auth record actions (`pocketbase-auth`)

Implemented:
- `GET /api/collections/{collection}/auth-methods`
  - supports `fields`
- `POST /api/collections/{collection}/auth-with-password`
  - supports body: `identity`, `password`, `identityField`
  - supports query: `expand`, `fields`
- `POST /api/collections/{collection}/auth-with-oauth2`
  - supports body: `provider`, `code`, `codeVerifier`, `redirectUrl`, `createData`
  - supports query: `expand`, `fields`
- `POST /api/collections/{collection}/auth-refresh`
  - supports query: `expand`, `fields`
- `POST /api/collections/{collection}/request-password-reset`
- `POST /api/collections/{collection}/confirm-password-reset`
- `POST /api/collections/{collection}/request-verification`
- `POST /api/collections/{collection}/confirm-verification`
- `POST /api/collections/{collection}/request-email-change`
- `POST /api/collections/{collection}/confirm-email-change`
- `POST /api/collections/{collection}/request-otp`
- `POST /api/collections/{collection}/auth-with-otp`
  - supports query: `expand`, `fields`
- `POST /api/collections/{collection}/impersonate/{id}`
  - supports body: `duration`
  - supports query: `expand`, `fields`

## Files API (`pocketbase-files`)

Implemented:
- URL helper for `GET /api/files/{collection}/{recordId}/{filename}`
  - supports `thumb`, `token`, `download`
- `POST /api/files/token`

## Realtime API (`pocketbase-realtime`)

Implemented:
- `GET /api/realtime` (SSE connect)
- `POST /api/realtime` (set subscriptions)
- automatic reconnect/backoff + listener wiring

## Collections/Admin services (`pocketbase-client`)

Implemented:
- Health: `GET /api/health`
- Collections: list/get/create/update/delete/truncate
  - list supports `page`, `perPage`, `sort`, `filter`, `fields`, `skipTotal`
  - getOne supports `fields`
- Backups: list/create/restore/delete
- Logs: list/get/stats
- Settings: get/update/test S3/test email
- Crons: list/run

## Notes

- Some endpoints require auth/superuser privileges per PocketBase rules.
- For multipart uploads in batch/records operations, payload structure follows PocketBase batch conventions.
