package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PocketBaseClientExtTest {
    @Test
    fun test_deserialize_decodesSuccessPayload() {
        val result: PocketBaseResult<TestPayload> = PocketBaseResult.Success(
            buildJsonObject {
                put("id", "r1")
                put("title", "Ship")
            },
        ).deserialize(TestClient.config.json)

        val success = result as PocketBaseResult.Success
        assertEquals("r1", success.value.id)
        assertEquals("Ship", success.value.title)
    }

    @Test
    fun test_deserialize_returnsFailureOnInvalidPayload() {
        val result: PocketBaseResult<TestPayload> = PocketBaseResult.Success(
            buildJsonObject { put("id", "r1") },
        ).deserialize(TestClient.config.json)

        assertTrue(result is PocketBaseResult.Failure)
    }

    @Test
    fun test_buildUrl_encodesQueryValues() {
        val sut = PocketBaseClientImpl(config = PocketBaseConfig("https://example.com"))
        val url = sut.buildUrl(
            path = "/api/collections/tasks/records",
            query = mapOf("filter" to "name = \"hello world\"", "page" to "1"),
        )

        assertTrue(url.contains("filter=name+%3D+%22hello+world%22"))
        assertTrue(url.contains("page=1"))
    }

    @Serializable
    private data class TestPayload(
        val id: String,
        val title: String,
    )

    private object TestClient : PocketBaseClient {
        override val config: PocketBaseConfig = PocketBaseConfig("http://localhost")
        override val authStore: AuthStore = AuthStore()

        override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
            PocketBaseResult.Failure(PocketBaseError(message = "unused"))

        override suspend fun post(path: String, body: kotlinx.serialization.json.JsonObject?): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
            PocketBaseResult.Failure(PocketBaseError(message = "unused"))

        override suspend fun put(path: String, body: kotlinx.serialization.json.JsonObject?): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
            PocketBaseResult.Failure(PocketBaseError(message = "unused"))

        override suspend fun patch(path: String, body: kotlinx.serialization.json.JsonObject?): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
            PocketBaseResult.Failure(PocketBaseError(message = "unused"))

        override suspend fun delete(path: String): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
            PocketBaseResult.Failure(PocketBaseError(message = "unused"))

        override suspend fun postMultipart(
            path: String,
            fields: Map<String, String>,
            files: List<MultipartFilePart>,
        ): PocketBaseResult<kotlinx.serialization.json.JsonObject> = PocketBaseResult.Failure(PocketBaseError(message = "unused"))

        override fun buildUrl(path: String, query: Map<String, String?>): String = "http://localhost"
    }
}
