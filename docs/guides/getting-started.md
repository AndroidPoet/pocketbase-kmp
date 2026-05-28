# Getting Started

## Dependencies

Add the modules you need:

```kotlin
implementation("io.github.androidpoet:pocketbase-client:<version>")
implementation("io.github.androidpoet:pocketbase-auth:<version>")
implementation("io.github.androidpoet:pocketbase-records:<version>")
implementation("io.github.androidpoet:pocketbase-files:<version>")
implementation("io.github.androidpoet:pocketbase-realtime:<version>")
```

## Create Client

```kotlin
val pb = PocketBase.create("http://127.0.0.1:8090")
```

## Create Feature Clients

```kotlin
val auth = createAuthClient(pb.client)
val records = createRecordsClient(pb.client)
val files = createFilesClient(pb.client)
val realtime = createRealtimeClient(pb.client)
```
