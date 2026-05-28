package io.github.androidpoet.pocketbase.realtime

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult

public interface RealtimeClient {
    public suspend fun connect(): PocketBaseResult<String>
    public suspend fun subscribe(topic: String): PocketBaseResult<Unit>
    public suspend fun unsubscribe(topic: String): PocketBaseResult<Unit>
    public suspend fun unsubscribeAll(): PocketBaseResult<Unit>
    public fun onEvent(listener: (event: String, data: String) -> Unit)
    public fun close()
}
