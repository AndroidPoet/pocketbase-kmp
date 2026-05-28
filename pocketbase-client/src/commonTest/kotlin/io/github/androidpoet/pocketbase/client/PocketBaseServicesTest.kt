package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PocketBaseServicesTest {
    @Test
    fun test_batch_send_postsToApiBatch() = runBlocking {
        val client = MockPocketBaseClient()
        val batch = BatchService(client)
        batch.create("tasks", buildJsonObject { put("title", "a") })
        val result = batch.send()
        assertTrue(result is PocketBaseResult.Success)
        assertEquals("/api/batch", client.lastPostPath)
    }

    @Test
    fun test_collections_truncate_usesDeleteEndpoint() = runBlocking {
        val client = MockPocketBaseClient()
        val sut = CollectionsService(client)
        sut.truncate("tasks")
        assertEquals("/api/collections/tasks/truncate", client.lastDeletePath)
    }

    @Test
    fun test_batch_send_withFiles_usesMultipartAndRewritesFields() = runBlocking {
        val client = MockPocketBaseClient()
        val batch = BatchService(client)
        batch.create(
            "tasks",
            buildJsonObject { put("title", "a") },
            fileParts = listOf(
                MultipartFilePart(
                    field = "avatar",
                    fileName = "a.png",
                    bytes = byteArrayOf(1, 2),
                    contentType = "image/png",
                ),
            ),
        )
        batch.send()
        assertEquals("/api/batch", client.lastMultipartPath)
        assertEquals("requests.0.avatar", client.lastMultipartFiles.first().field)
    }
}

private class MockPocketBaseClient : PocketBaseClient {
    override val config: PocketBaseConfig = PocketBaseConfig("http://localhost")
    override val authStore: AuthStore = AuthStore()
    var lastPostPath: String? = null
    var lastDeletePath: String? = null
    var lastMultipartPath: String? = null
    var lastMultipartFiles: List<MultipartFilePart> = emptyList()

    override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun post(path: String, body: JsonObject?): PocketBaseResult<JsonObject> {
        lastPostPath = path
        return PocketBaseResult.Success(buildJsonObject {})
    }
    override suspend fun put(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun patch(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun delete(path: String): PocketBaseResult<JsonObject> {
        lastDeletePath = path
        return PocketBaseResult.Success(buildJsonObject {})
    }
    override suspend fun postMultipart(
        path: String,
        fields: Map<String, String>,
        files: List<MultipartFilePart>,
    ): PocketBaseResult<JsonObject> {
        lastMultipartPath = path
        lastMultipartFiles = files
        return PocketBaseResult.Success(buildJsonObject {})
    }
    override fun buildUrl(path: String, query: Map<String, String?>): String = "http://localhost$path"
}
