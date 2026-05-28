package io.github.androidpoet.pocketbase.records

import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.core.models.PocketBaseListResponse
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

public class RecordsClientImpl(
    private val client: PocketBaseClient,
) : RecordsClient {
    override suspend fun getList(
        collectionIdOrName: String,
        page: Int,
        perPage: Int,
        filter: String?,
    ): PocketBaseResult<PocketBaseListResponse> {
        val res = client.get(
            "/api/collections/$collectionIdOrName/records",
            mapOf("page" to page.toString(), "perPage" to perPage.toString(), "filter" to filter),
        )
        return when (res) {
            is PocketBaseResult.Success -> {
                val v = res.value
                val items = v["items"]?.jsonArray?.mapNotNull { it as? JsonObject } ?: emptyList()
                PocketBaseResult.Success(
                    PocketBaseListResponse(
                        page = v["page"]?.jsonPrimitive?.int ?: 1,
                        perPage = v["perPage"]?.jsonPrimitive?.int ?: perPage,
                        totalItems = v["totalItems"]?.jsonPrimitive?.int ?: items.size,
                        totalPages = v["totalPages"]?.jsonPrimitive?.int ?: 1,
                        items = items,
                    ),
                )
            }
            is PocketBaseResult.Failure -> res
        }
    }

    override suspend fun getFullList(
        collectionIdOrName: String,
        batch: Int,
        filter: String?,
    ): PocketBaseResult<List<JsonObject>> {
        val aggregated = mutableListOf<JsonObject>()
        var page = 1
        while (true) {
            when (val res = getList(collectionIdOrName, page = page, perPage = batch, filter = filter)) {
                is PocketBaseResult.Failure -> return res
                is PocketBaseResult.Success -> {
                    aggregated += res.value.items
                    if (page >= res.value.totalPages) return PocketBaseResult.Success(aggregated)
                    page += 1
                }
            }
        }
    }

    override suspend fun getOne(collectionIdOrName: String, id: String): PocketBaseResult<JsonObject> =
        client.get("/api/collections/$collectionIdOrName/records/$id")

    override suspend fun create(collectionIdOrName: String, body: JsonObject): PocketBaseResult<JsonObject> =
        client.post("/api/collections/$collectionIdOrName/records", body)

    override suspend fun update(collectionIdOrName: String, id: String, body: JsonObject): PocketBaseResult<JsonObject> =
        client.patch("/api/collections/$collectionIdOrName/records/$id", body)

    override suspend fun upsert(collectionIdOrName: String, body: JsonObject): PocketBaseResult<JsonObject> =
        client.put("/api/collections/$collectionIdOrName/records", body)

    override suspend fun delete(collectionIdOrName: String, id: String): PocketBaseResult<JsonObject> =
        client.delete("/api/collections/$collectionIdOrName/records/$id")
}
