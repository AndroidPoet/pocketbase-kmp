# Files Guide

```kotlin
val files = createFilesClient(pb.client)
```

## File Token

```kotlin
files.getToken()
```

## Build File URL

```kotlin
files.getUrl(
  collectionIdOrName = "tasks",
  recordId = "RECORD_ID",
  filename = "image.png",
  thumb = "100x100",
)
```
