package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import io.github.androidpoet.pocketbase.core.result.toPocketBaseResult
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

public suspend inline fun <reified T> PocketBaseClient.getTyped(
    path: String,
    query: Map<String, String?> = emptyMap(),
    json: Json = config.json,
): PocketBaseResult<T> = get(path, query).deserialize(json)

public suspend inline fun <reified T> PocketBaseClient.postTyped(
    path: String,
    body: JsonObject? = null,
    json: Json = config.json,
): PocketBaseResult<T> = post(path, body).deserialize(json)

public suspend inline fun <reified T> PocketBaseClient.patchTyped(
    path: String,
    body: JsonObject? = null,
    json: Json = config.json,
): PocketBaseResult<T> = patch(path, body).deserialize(json)

public suspend inline fun <reified T> PocketBaseClient.putTyped(
    path: String,
    body: JsonObject? = null,
    json: Json = config.json,
): PocketBaseResult<T> = put(path, body).deserialize(json)

public inline fun <reified T> PocketBaseResult<JsonObject>.deserialize(
    json: Json,
): PocketBaseResult<T> = when (this) {
    is PocketBaseResult.Success -> runCatching {
        json.decodeFromJsonElement<T>(value)
    }.toPocketBaseResult { throwable ->
        PocketBaseError(
            message = throwable.message ?: "Failed to deserialize PocketBase response",
            cause = throwable,
        )
    }
    is PocketBaseResult.Failure -> this
}

public fun <T> PocketBaseResult<JsonObject>.deserialize(
    deserializer: DeserializationStrategy<T>,
    json: Json,
): PocketBaseResult<T> = when (this) {
    is PocketBaseResult.Success -> runCatching {
        json.decodeFromJsonElement(deserializer, value)
    }.toPocketBaseResult { throwable ->
        PocketBaseError(
            message = throwable.message ?: "Failed to deserialize PocketBase response",
            cause = throwable,
        )
    }
    is PocketBaseResult.Failure -> this
}
