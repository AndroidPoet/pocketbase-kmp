# pocketbase-kmp

Kotlin Multiplatform SDK for PocketBase, built with the same modular architecture style as this workspace's `supabase-kmp`.

## Modules

- `pocketbase-core`: result/error/model primitives.
- `pocketbase-client`: HTTP client, auth store, and admin services (`health`, `collections`, `batch`, `backups`, `logs`, `settings`, `crons`).
- `pocketbase-auth`: record auth flows (`auth-with-password`, `auth-refresh`, OAuth2 code, OTP, email/password reset/verification, impersonate).
- `pocketbase-records`: records CRUD, list, and full-list pagination.
- `pocketbase-files`: file URL generation and file token.
- `pocketbase-realtime`: API scaffold for realtime subscriptions (transport implementation is currently beta).

## Quick Start

```kotlin
val pb = PocketBase.create("http://127.0.0.1:8090")
val records = createRecordsClient(pb.client)
val auth = createAuthClient(pb.client)
val files = createFilesClient(pb.client)
```

## Service Coverage (vs dart-sdk)

- Covered now: collections, records CRUD/full-list, batch (JSON), files URL/token, auth record flows, health, logs, backups, settings, crons.
- Pending for strict parity: realtime protocol-complete transport and multipart batch/files parity edge cases.

## Tests

Run:

```bash
./gradlew test
```
