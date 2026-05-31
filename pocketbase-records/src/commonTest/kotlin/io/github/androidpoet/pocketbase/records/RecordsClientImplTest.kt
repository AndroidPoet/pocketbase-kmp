package io.github.androidpoet.pocketbase.records

import io.github.androidpoet.pocketbase.client.AuthStore
import io.github.androidpoet.pocketbase.client.MultipartFilePart
import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.client.PocketBaseConfig
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordsClientImplTest {
    @Test
    fun test_getFullList_paginated_returnsMergedResults() = runBlocking {
        val client = MockPocketBaseClient()
        client.getQueue += listResponse(page = 1, totalPages = 2, "1")
        client.getQueue += listResponse(page = 2, totalPages = 2, "2")
        val sut = RecordsClientImpl(client)

        val result = sut.getFullList("tasks", batch = 1)

        assertTrue(result is PocketBaseResult.Success)
        assertEquals(2, result.value.size)
        assertEquals("/api/collections/tasks/records", client.lastGetPath)
    }

    @Test
    fun test_upsert_callsPutRecordsEndpoint() = runBlocking {
        val client = MockPocketBaseClient()
        val sut = RecordsClientImpl(client)
        sut.upsert("tasks", buildJsonObject { put("title", "x") })
        assertEquals("/api/collections/tasks/records", client.lastPutPath)
    }

    @Test
    fun test_getFirstListItem_returnsFirstItem() = runBlocking {
        val client = MockPocketBaseClient()
        client.getQueue += listResponse(page = 1, totalPages = 1, "first")
        val sut = RecordsClientImpl(client)

        val result = sut.getFirstListItem(collectionIdOrName = "tasks", filter = "status='open'")

        assertTrue(result is PocketBaseResult.Success)
        assertEquals("first", result.value["id"]?.toString()?.trim('"'))
    }

    @Test
    fun test_getFirstListItem_returns404WhenNoItemsFound() = runBlocking {
        val client = MockPocketBaseClient()
        client.getQueue += buildJsonObject {
            put("page", 1)
            put("perPage", 1)
            put("totalItems", 0)
            put("totalPages", 1)
            put("items", buildJsonArray {})
        }
        val sut = RecordsClientImpl(client)

        val result = sut.getFirstListItem(collectionIdOrName = "tasks", filter = "status='missing'")

        assertTrue(result is PocketBaseResult.Failure)
        assertEquals(404, result.error.statusCode)
        assertTrue(result.error.message.isNotBlank())
    }

    private fun listResponse(page: Int, totalPages: Int, id: String): JsonObject = buildJsonObject {
        put("page", page)
        put("perPage", 1)
        put("totalItems", totalPages)
        put("totalPages", totalPages)
        put("items", buildJsonArray { add(buildJsonObject { put("id", id) }) })
    }
}

private class MockPocketBaseClient : PocketBaseClient {
    override val config: PocketBaseConfig = PocketBaseConfig("http://localhost")
    override val authStore: AuthStore = AuthStore()
    val getQueue: ArrayDeque<JsonObject> = ArrayDeque()
    var lastGetPath: String? = null
    var lastPutPath: String? = null

    override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<JsonObject> {
        lastGetPath = path
        return PocketBaseResult.Success(getQueue.removeFirst())
    }

    override suspend fun post(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun put(path: String, body: JsonObject?): PocketBaseResult<JsonObject> {
        lastPutPath = path
        return PocketBaseResult.Success(buildJsonObject {})
    }
    override suspend fun patch(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun delete(path: String): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun postMultipart(
        path: String,
        fields: Map<String, String>,
        files: List<MultipartFilePart>,
    ): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override fun buildUrl(path: String, query: Map<String, String?>): String = "http://localhost$path"
}
