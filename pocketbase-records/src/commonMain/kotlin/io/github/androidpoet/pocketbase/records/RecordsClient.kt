package io.github.androidpoet.pocketbase.records

import io.github.androidpoet.pocketbase.core.models.PocketBaseListResponse
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.JsonObject

public interface RecordsClient {
    public suspend fun getList(collectionIdOrName: String, page: Int = 1, perPage: Int = 30, filter: String? = null): PocketBaseResult<PocketBaseListResponse>
    public suspend fun getFullList(collectionIdOrName: String, batch: Int = 200, filter: String? = null): PocketBaseResult<List<JsonObject>>
    public suspend fun getOne(collectionIdOrName: String, id: String): PocketBaseResult<JsonObject>
    public suspend fun create(collectionIdOrName: String, body: JsonObject): PocketBaseResult<JsonObject>
    public suspend fun update(collectionIdOrName: String, id: String, body: JsonObject): PocketBaseResult<JsonObject>
    public suspend fun upsert(collectionIdOrName: String, body: JsonObject): PocketBaseResult<JsonObject>
    public suspend fun delete(collectionIdOrName: String, id: String): PocketBaseResult<JsonObject>
}
