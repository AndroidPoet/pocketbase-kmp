package io.github.androidpoet.pocketbase.records

import io.github.androidpoet.pocketbase.core.models.PocketBaseListResponse
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.JsonObject

public interface RecordsClient {
    public suspend fun getList(
        collectionIdOrName: String,
        page: Int = 1,
        perPage: Int = 30,
        filter: String? = null,
        sort: String? = null,
        expand: String? = null,
        fields: String? = null,
        skipTotal: Boolean? = null,
    ): PocketBaseResult<PocketBaseListResponse>
    public suspend fun getFullList(
        collectionIdOrName: String,
        batch: Int = 200,
        filter: String? = null,
        sort: String? = null,
        expand: String? = null,
        fields: String? = null,
        skipTotal: Boolean? = null,
    ): PocketBaseResult<List<JsonObject>>
    public suspend fun getOne(collectionIdOrName: String, id: String, expand: String? = null, fields: String? = null): PocketBaseResult<JsonObject>
    public suspend fun create(collectionIdOrName: String, body: JsonObject, expand: String? = null, fields: String? = null): PocketBaseResult<JsonObject>
    public suspend fun update(collectionIdOrName: String, id: String, body: JsonObject, expand: String? = null, fields: String? = null): PocketBaseResult<JsonObject>
    public suspend fun upsert(collectionIdOrName: String, body: JsonObject): PocketBaseResult<JsonObject>
    public suspend fun delete(collectionIdOrName: String, id: String): PocketBaseResult<JsonObject>
}
