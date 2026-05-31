<p align="center">
  <img src="docs/assets/images/header.png" width="920" alt="PocketBase KMP">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.1.10-blue.svg?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Ktor-3.1.1-blue.svg" alt="Ktor">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20macOS%20%7C%20tvOS%20%7C%20watchOS%20%7C%20JVM%20%7C%20Linux%20%7C%20Windows%20%7C%20WasmJs-green.svg" alt="Platforms">
  <img src="https://img.shields.io/badge/Maven%20Central-0.3.3-blue.svg" alt="Maven Central">
  <img src="https://img.shields.io/badge/License-MIT-orange.svg" alt="License">
</p>

# PocketBase KMP

Kotlin Multiplatform SDK for [PocketBase](https://pocketbase.io) with modular clients, coroutine-first APIs, and result-based error handling.

## Features

- **Modular SDK design** - `core`, `client`, `auth`, `records`, `files`, `realtime`
- **Result monad** - `PocketBaseResult<T>` with `map`, `flatMap`, `recover`, `getOrElse`
- **Cancellation-safe** - coroutine `CancellationException` is rethrown, never swallowed
- **Typed decoding helpers** - `getTyped<T>()`, `postTyped<T>()`, `deserialize<T>()`
- **Admin + data services** - health, collections, batch, backups, logs, settings, crons
- **Auth workflows** - password, OAuth2 code flow, OTP, refresh, reset, verification, impersonation
- **Realtime subscriptions** - connect, subscribe, unsubscribe, auto-reconnect backoff
- **15 platform targets** - Android, iOS, macOS, tvOS, watchOS, JVM, Linux, Windows, WasmJs

## Setup

Add the dependencies you need:

```kotlin
[versions]
pocketbase-kmp = "0.3.3"

[libraries]
pocketbase-core = { module = "io.github.androidpoet:pocketbase-core", version.ref = "pocketbase-kmp" }
pocketbase-client = { module = "io.github.androidpoet:pocketbase-client", version.ref = "pocketbase-kmp" }
pocketbase-auth = { module = "io.github.androidpoet:pocketbase-auth", version.ref = "pocketbase-kmp" }
pocketbase-records = { module = "io.github.androidpoet:pocketbase-records", version.ref = "pocketbase-kmp" }
pocketbase-files = { module = "io.github.androidpoet:pocketbase-files", version.ref = "pocketbase-kmp" }
pocketbase-realtime = { module = "io.github.androidpoet:pocketbase-realtime", version.ref = "pocketbase-kmp" }
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.pocketbase.client)
            implementation(libs.pocketbase.auth)
            implementation(libs.pocketbase.records)
            implementation(libs.pocketbase.files)
            implementation(libs.pocketbase.realtime)
        }
    }
}
```

## Usage

### Create a Client

```kotlin
val pb = PocketBase.create(baseUrl = "http://127.0.0.1:8090")

val auth = createAuthClient(pb.client)
val records = createRecordsClient(pb.client)
val files = createFilesClient(pb.client)
val realtime = createRealtimeClient(pb.client)
```

### Records

```kotlin
val listResult = records.getList(
    collectionIdOrName = "tasks",
    page = 1,
    perPage = 20,
    filter = "status = 'open'",
)

listResult.onSuccess { page ->
    println("items=${page.items.size}, total=${page.totalItems}")
}.onFailure { error ->
    println("error=${error.message}")
}

val fullList = records.getFullList("tasks", batch = 200)
```

### Typed Responses

```kotlin
@Serializable
data class HealthResponse(val code: Int, val message: String)

val health: PocketBaseResult<HealthResponse> = pb.client.getTyped("/api/health")
```

### Auth

```kotlin
auth.authWithPassword(
    collectionIdOrName = "users",
    identity = "user@example.com",
    password = "secret",
).onSuccess { session ->
    println("token=${session.token}")
}

auth.refresh("users")
auth.requestPasswordReset("users", "user@example.com")
auth.requestVerification("users", "user@example.com")
```

Notes from official PocketBase behavior:

- OTP auth requires enabling the OTP option in the target auth collection.
- OAuth2 code exchange requires provider configuration and redirect flow setup in PocketBase.

### Files

```kotlin
val token = files.getToken()
val url = files.getUrl(
    collectionIdOrName = "tasks",
    recordId = "RECORD_ID",
    filename = "photo.png",
)
```

### Realtime

```kotlin
realtime.onEvent { event, data ->
    println("$event -> $data")
}

realtime.connect()
realtime.subscribe("collections/tasks")
```

Realtime note:

- Keep the app process active while listening for realtime events.

### Result Helpers

```kotlin
val value = PocketBaseResult.catching { 42 }
    .map { it * 2 }
    .getOrElse { -1 }

val kotlinResult = value.let { PocketBaseResult.Success(it) }.toKotlinResult()
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                              Your App                               │
├──────────┬────────────┬───────────┬───────────┬───────────┬────────┤
│auth      │records     │files      │realtime   │services   │client  │
│password  │CRUD/list   │token/url  │subscribe  │health/logs│HTTP    │
│oauth2/otp│full-list   │helpers    │reconnect  │settings   │auth    │
├──────────┴────────────┴───────────┴───────────┴───────────┼────────┤
│                         pocketbase-core                      │        │
│           PocketBaseResult · errors · response models        │        │
└───────────────────────────────────────────────────────────────┴────────┘
```

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `pocketbase-core` | `io.github.androidpoet:pocketbase-core` | Result type, error model, core response models |
| `pocketbase-client` | `io.github.androidpoet:pocketbase-client` | HTTP transport, config, auth store, admin services |
| `pocketbase-auth` | `io.github.androidpoet:pocketbase-auth` | Record auth and account lifecycle endpoints |
| `pocketbase-records` | `io.github.androidpoet:pocketbase-records` | Record CRUD + paginated/full-list helpers |
| `pocketbase-files` | `io.github.androidpoet:pocketbase-files` | File token and URL helper operations |
| `pocketbase-realtime` | `io.github.androidpoet:pocketbase-realtime` | Realtime connect/subscribe/unsubscribe stream client |

Detailed endpoint parity: [docs/api-coverage.md](docs/api-coverage.md)

## Targets

| Platform | Target |
|----------|--------|
| Android | `androidTarget()` |
| JVM | `jvm()` |
| iOS | `iosX64()` `iosArm64()` `iosSimulatorArm64()` |
| macOS | `macosX64()` `macosArm64()` |
| tvOS | `tvosX64()` `tvosArm64()` `tvosSimulatorArm64()` |
| watchOS | `watchosX64()` `watchosArm64()` `watchosSimulatorArm64()` |
| Linux | `linuxX64()` |
| Windows | `mingwX64()` |
| Web | `wasmJs()` |

## Testing

```bash
./gradlew checkJvm
```

Direct per-task alternative:

```bash
./gradlew jvmTest
```

Avoid running `./gradlew test` or `allTests` for local verification unless you intentionally want Android/native target tasks as well.

## Compatibility Notes

- Batch endpoint support depends on PocketBase server settings (`/api/batch` must be enabled).
- Some admin APIs require superuser authentication based on PocketBase API rules.
- PocketBase is pre-1.0 and may introduce API changes between server versions.

## License

MIT. See [LICENSE](LICENSE).
