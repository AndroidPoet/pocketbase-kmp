package io.github.androidpoet.pocketbase.auth

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

class AuthClientImplTest {
    @Test
    fun test_authWithPassword_savesTokenInAuthStore() = runBlocking {
        val client = MockPocketBaseClient()
        client.nextPost = buildJsonObject {
            put("token", "abc")
            put("record", buildJsonObject { put("id", "1") })
        }
        val sut = AuthClientImpl(client)

        val result = sut.authWithPassword("users", "e@x.com", "pass")

        assertTrue(result is PocketBaseResult.Success)
        assertEquals("abc", client.authStore.token)
        assertEquals("/api/collections/users/auth-with-password", client.lastPostPath)
    }
}

private class MockPocketBaseClient : PocketBaseClient {
    override val config: PocketBaseConfig = PocketBaseConfig("http://localhost")
    override val authStore: AuthStore = AuthStore()
    var nextPost: JsonObject = buildJsonObject {}
    var lastPostPath: String? = null

    override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun post(path: String, body: JsonObject?): PocketBaseResult<JsonObject> {
        lastPostPath = path
        return PocketBaseResult.Success(nextPost)
    }
    override suspend fun put(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun patch(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun delete(path: String): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override suspend fun postMultipart(
        path: String,
        fields: Map<String, String>,
        files: List<MultipartFilePart>,
    ): PocketBaseResult<JsonObject> = PocketBaseResult.Success(buildJsonObject {})
    override fun buildUrl(path: String, query: Map<String, String?>): String = "http://localhost$path"
}
