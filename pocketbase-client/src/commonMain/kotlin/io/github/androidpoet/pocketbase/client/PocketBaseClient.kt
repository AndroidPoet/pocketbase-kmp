package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.JsonObject

public data class MultipartFilePart(
    val field: String,
    val fileName: String,
    val bytes: ByteArray,
    val contentType: String = "application/octet-stream",
)

public interface PocketBaseClient {
    public val config: PocketBaseConfig
    public val authStore: AuthStore

    public suspend fun get(path: String, query: Map<String, String?> = emptyMap()): PocketBaseResult<JsonObject>
    public suspend fun post(path: String, body: JsonObject? = null): PocketBaseResult<JsonObject>
    public suspend fun put(path: String, body: JsonObject? = null): PocketBaseResult<JsonObject>
    public suspend fun patch(path: String, body: JsonObject? = null): PocketBaseResult<JsonObject>
    public suspend fun delete(path: String): PocketBaseResult<JsonObject>
    public suspend fun postMultipart(
        path: String,
        fields: Map<String, String> = emptyMap(),
        files: List<MultipartFilePart> = emptyList(),
    ): PocketBaseResult<JsonObject>
    public fun buildUrl(path: String, query: Map<String, String?> = emptyMap()): String
}
