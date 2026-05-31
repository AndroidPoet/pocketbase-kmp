package io.github.androidpoet.pocketbase.records

import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.core.models.PocketBaseListResponse
import io.github.androidpoet.pocketbase.core.result.PocketBaseError
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
        sort: String?,
        expand: String?,
        fields: String?,
        skipTotal: Boolean?,
    ): PocketBaseResult<PocketBaseListResponse> {
        val res = client.get(
            "/api/collections/$collectionIdOrName/records",
            mapOf(
                "page" to page.toString(),
                "perPage" to perPage.toString(),
                "filter" to filter,
                "sort" to sort,
                "expand" to expand,
                "fields" to fields,
                "skipTotal" to skipTotal?.toString(),
            ),
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
        sort: String?,
        expand: String?,
        fields: String?,
        skipTotal: Boolean,
    ): PocketBaseResult<List<JsonObject>> {
        val aggregated = mutableListOf<JsonObject>()
        var page = 1
        while (true) {
            when (
                val res = getList(
                    collectionIdOrName = collectionIdOrName,
                    page = page,
                    perPage = batch,
                    filter = filter,
                    sort = sort,
                    expand = expand,
                    fields = fields,
                    skipTotal = skipTotal,
                )
            ) {
                is PocketBaseResult.Failure -> return res
                is PocketBaseResult.Success -> {
                    val items = res.value.items
                    aggregated += items
                    if (items.size < batch || page >= res.value.totalPages) return PocketBaseResult.Success(aggregated)
                    page += 1
                }
            }
        }
    }

    override suspend fun getOne(collectionIdOrName: String, id: String, expand: String?, fields: String?): PocketBaseResult<JsonObject> =
        client.get("/api/collections/$collectionIdOrName/records/$id", mapOf("expand" to expand, "fields" to fields))

    override suspend fun getFirstListItem(
        collectionIdOrName: String,
        filter: String,
        expand: String?,
        fields: String?,
    ): PocketBaseResult<JsonObject> {
        val listResult = getList(
            collectionIdOrName = collectionIdOrName,
            page = 1,
            perPage = 1,
            filter = filter,
            expand = expand,
            fields = fields,
            skipTotal = true,
        )
        return when (listResult) {
            is PocketBaseResult.Failure -> listResult
            is PocketBaseResult.Success -> {
                val firstItem = listResult.value.items.firstOrNull()
                if (firstItem != null) {
                    PocketBaseResult.Success(firstItem)
                } else {
                    PocketBaseResult.Failure(
                        PocketBaseError(
                            statusCode = 404,
                            message = "The requested resource wasn't found.",
                        ),
                    )
                }
            }
        }
    }

    override suspend fun create(collectionIdOrName: String, body: JsonObject, expand: String?, fields: String?): PocketBaseResult<JsonObject> =
        client.post(pathWithQuery("/api/collections/$collectionIdOrName/records", "expand" to expand, "fields" to fields), body)

    override suspend fun update(collectionIdOrName: String, id: String, body: JsonObject, expand: String?, fields: String?): PocketBaseResult<JsonObject> =
        client.patch(pathWithQuery("/api/collections/$collectionIdOrName/records/$id", "expand" to expand, "fields" to fields), body)

    override suspend fun upsert(collectionIdOrName: String, body: JsonObject): PocketBaseResult<JsonObject> =
        client.put("/api/collections/$collectionIdOrName/records", body)

    override suspend fun delete(collectionIdOrName: String, id: String): PocketBaseResult<JsonObject> =
        client.delete("/api/collections/$collectionIdOrName/records/$id")

    private fun pathWithQuery(path: String, vararg params: Pair<String, String?>): String {
        val encoded = params
            .filter { it.second != null }
            .joinToString("&") { "${it.first}=${it.second}" }
        return if (encoded.isBlank()) path else "$path?$encoded"
    }
}
