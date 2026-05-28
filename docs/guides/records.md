# Records Guide

```kotlin
val records = createRecordsClient(pb.client)
```

## CRUD

```kotlin
records.create("tasks", body)
records.getOne("tasks", "RECORD_ID")
records.update("tasks", "RECORD_ID", body)
records.delete("tasks", "RECORD_ID")
```

## List and Full List

```kotlin
records.getList("tasks", page = 1, perPage = 30, filter = "done = false")
records.getFullList("tasks", batch = 200)
```

## Upsert

```kotlin
records.upsert("tasks", body)
```
