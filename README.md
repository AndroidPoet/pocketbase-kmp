![PocketBase KMP Header](docs/assets/images/header.png)

# PocketBase KMP

Kotlin Multiplatform SDK for PocketBase with a modular architecture (same style as `supabase-kmp`).

## Highlights

- KMP-first module split (`core`, `client`, `auth`, `records`, `files`, `realtime`)
- Coroutine-based APIs
- Result-based error handling (`PocketBaseResult`)
- Service-oriented client with admin and data operations
- CI/CD + Maven Central publish workflow templates included

## Install

```kotlin
[versions]
pocketbase-kmp = "0.3.1"

[libraries]
pocketbase-client = { module = "io.github.androidpoet:pocketbase-client", version.ref = "pocketbase-kmp" }
pocketbase-auth = { module = "io.github.androidpoet:pocketbase-auth", version.ref = "pocketbase-kmp" }
pocketbase-records = { module = "io.github.androidpoet:pocketbase-records", version.ref = "pocketbase-kmp" }
pocketbase-files = { module = "io.github.androidpoet:pocketbase-files", version.ref = "pocketbase-kmp" }
pocketbase-realtime = { module = "io.github.androidpoet:pocketbase-realtime", version.ref = "pocketbase-kmp" }
```

## Quick Start

```kotlin
val pb = PocketBase.create("http://127.0.0.1:8090")

val auth = createAuthClient(pb.client)
val records = createRecordsClient(pb.client)
val files = createFilesClient(pb.client)
val realtime = createRealtimeClient(pb.client)
```

## Module Overview

- `pocketbase-core`: shared result and core models
- `pocketbase-client`: transport, auth store, and admin services
- `pocketbase-auth`: record-auth endpoints and auth flows
- `pocketbase-records`: CRUD, list, and full-list pagination
- `pocketbase-files`: file token + file URL helpers
- `pocketbase-realtime`: realtime connection, subscribe/unsubscribe, reconnect strategy

## Implemented Services

- Health: `/api/health`
- Collections: CRUD + truncate
- Records: list/full-list/get/create/update/upsert/delete
- Batch: JSON + multipart file request mapping
- Files: token + URL helpers
- Auth: methods/password refresh/OAuth2 code/OTP/reset/verify/email-change/impersonate
- Logs: list/get/stats
- Backups: list/create/restore/delete
- Settings: get/update/test endpoints
- Crons: list/run
- Realtime: connect/subscribe/unsubscribe/unsubscribeAll/event listener/reconnect

## Testing

```bash
./gradlew jvmTest
```

Note: `./gradlew test` also runs Android unit-test tasks and requires local Android SDK configuration.
