package io.github.androidpoet.pocketbase.realtime

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

class RealtimeClientImplTest {
    @Test
    fun test_subscribe_postsClientIdAndSubscriptions() = runBlocking {
        val client = MockPocketBaseClient()
        val sut = RealtimeClientImpl(client, enableStreaming = false, maxRetryAttempts = 0)

        val result = sut.subscribe("tasks/*")

        assertTrue(result is PocketBaseResult.Success)
        assertEquals("/api/realtime", client.lastPostPath)
        assertEquals("cid-1", client.lastPostedBody?.get("clientId")?.toString()?.trim('"'))
        assertEquals("[\"tasks/*\"]", client.lastPostedBody?.get("subscriptions").toString())
    }

    @Test
    fun test_retryDelay_usesSteppedSchedule() {
        val sut = RealtimeClientImpl(MockPocketBaseClient(), enableStreaming = false, maxRetryAttempts = 0)
        assertEquals(200, sut.nextRetryDelayMs(1))
        assertEquals(300, sut.nextRetryDelayMs(2))
        assertEquals(2000, sut.nextRetryDelayMs(100))
    }
}

private class MockPocketBaseClient : PocketBaseClient {
    override val config: PocketBaseConfig = PocketBaseConfig("http://localhost")
    override val authStore: AuthStore = AuthStore()
    var lastPostPath: String? = null
    var lastPostedBody: JsonObject? = null

    override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<JsonObject> =
        PocketBaseResult.Success(buildJsonObject { put("clientId", "cid-1") })

    override suspend fun post(path: String, body: JsonObject?): PocketBaseResult<JsonObject> {
        lastPostPath = path
        lastPostedBody = body
        return PocketBaseResult.Success(buildJsonObject {})
    }

    override suspend fun put(path: String, body: JsonObject?): PocketBaseResult<JsonObject> =
        PocketBaseResult.Success(buildJsonObject {})

    override suspend fun patch(path: String, body: JsonObject?): PocketBaseResult<JsonObject> =
        PocketBaseResult.Success(buildJsonObject {})

    override suspend fun delete(path: String): PocketBaseResult<JsonObject> =
        PocketBaseResult.Success(buildJsonObject {})

    override suspend fun postMultipart(
        path: String,
        fields: Map<String, String>,
        files: List<MultipartFilePart>,
    ): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})

    override fun buildUrl(path: String, query: Map<String, String?>): String = "http://localhost$path"
}
