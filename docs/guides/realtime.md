# Realtime Guide

```kotlin
val realtime = createRealtimeClient(pb.client)
```

## Subscribe

```kotlin
realtime.onEvent { event, data ->
  println("$event -> $data")
}

realtime.subscribe("tasks/*")
```

## Unsubscribe

```kotlin
realtime.unsubscribe("tasks/*")
realtime.unsubscribeAll()
```

## Notes

- Uses `/api/realtime` client id bootstrap.
- Includes reconnect with stepped backoff and resubscribe.
