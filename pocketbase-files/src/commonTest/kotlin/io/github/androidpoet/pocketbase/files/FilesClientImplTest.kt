package io.github.androidpoet.pocketbase.files

import io.github.androidpoet.pocketbase.client.AuthStore
import io.github.androidpoet.pocketbase.client.MultipartFilePart
import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.client.PocketBaseConfig
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilesClientImplTest {
    @Test
    fun test_getUrl_buildsPath() {
        val client = MockPocketBaseClient()
        val sut = FilesClientImpl(client)
        val url = sut.getUrl("tasks", "1", "a.png", "100x100")
        assertEquals("http://localhost/api/files/tasks/1/a.png?thumb=100x100", url)
    }

    @Test
    fun test_getToken_returnsToken() = runBlocking {
        val client = MockPocketBaseClient()
        val sut = FilesClientImpl(client)
        val result = sut.getToken()
        assertTrue(result is PocketBaseResult.Success)
        assertEquals("tok", result.value.token)
    }
}

private class MockPocketBaseClient : PocketBaseClient {
    override val config: PocketBaseConfig = PocketBaseConfig("http://localhost")
    override val authStore: AuthStore = AuthStore()
    override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun post(path: String, body: JsonObject?): PocketBaseResult<JsonObject> =
        PocketBaseResult.Success(buildJsonObject { put("token", "tok") })
    override suspend fun put(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun patch(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun delete(path: String): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun postMultipart(
        path: String,
        fields: Map<String, String>,
        files: List<MultipartFilePart>,
    ): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override fun buildUrl(path: String, query: Map<String, String?>): String {
        val qp = query.entries.filter { it.value != null }.joinToString("&") { "${it.key}=${it.value}" }
        return if (qp.isEmpty()) "http://localhost$path" else "http://localhost$path?$qp"
    }
}
