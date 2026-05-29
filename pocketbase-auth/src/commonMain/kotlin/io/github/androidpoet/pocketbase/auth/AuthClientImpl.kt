package io.github.androidpoet.pocketbase.auth

import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

public class AuthClientImpl(
    private val client: PocketBaseClient,
) : AuthClient {
    override suspend fun authMethods(collectionIdOrName: String, fields: String?): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
        client.get("/api/collections/$collectionIdOrName/auth-methods", mapOf("fields" to fields))

    override suspend fun authWithPassword(
        collectionIdOrName: String,
        identity: String,
        password: String,
        identityField: String?,
        expand: String?,
        fields: String?,
    ): PocketBaseResult<RecordAuth> {
        val path = pathWithQuery(
            "/api/collections/$collectionIdOrName/auth-with-password",
            "expand" to expand,
            "fields" to fields,
        )
        val body = buildJsonObject {
            put("identity", identity)
            put("password", password)
            identityField?.let { put("identityField", it) }
        }
        return when (val res = client.post(path, body)) {
            is PocketBaseResult.Success -> {
                val token = res.value["token"]?.jsonPrimitive?.contentOrNull
                if (token == null) PocketBaseResult.Failure(PocketBaseError(message = "Missing token in response"))
                else {
                    client.authStore.save(token, res.value["record"]?.jsonObject)
                    PocketBaseResult.Success(RecordAuth(token))
                }
            }
            is PocketBaseResult.Failure -> res
        }
    }

    override suspend fun refresh(collectionIdOrName: String, expand: String?, fields: String?): PocketBaseResult<RecordAuth> {
        val path = pathWithQuery(
            "/api/collections/$collectionIdOrName/auth-refresh",
            "expand" to expand,
            "fields" to fields,
        )
        return when (val res = client.post(path, null)) {
            is PocketBaseResult.Success -> {
                val token = res.value["token"]?.jsonPrimitive?.contentOrNull
                if (token == null) PocketBaseResult.Failure(PocketBaseError(message = "Missing token in response"))
                else {
                    client.authStore.save(token, res.value["record"]?.jsonObject)
                    PocketBaseResult.Success(RecordAuth(token))
                }
            }
            is PocketBaseResult.Failure -> res
        }
    }

    override suspend fun authWithOAuth2Code(
        collectionIdOrName: String,
        provider: String,
        code: String,
        codeVerifier: String?,
        redirectUrl: String?,
        createData: kotlinx.serialization.json.JsonObject?,
        expand: String?,
        fields: String?,
    ): PocketBaseResult<RecordAuth> {
        val body = buildJsonObject {
            put("provider", provider)
            put("code", code)
            codeVerifier?.let { put("codeVerifier", it) }
            redirectUrl?.let { put("redirectUrl", it) }
            createData?.let { put("createData", it) }
        }
        val path = pathWithQuery(
            "/api/collections/$collectionIdOrName/auth-with-oauth2",
            "expand" to expand,
            "fields" to fields,
        )
        return when (val res = client.post(path, body)) {
            is PocketBaseResult.Success -> {
                val token = res.value["token"]?.jsonPrimitive?.contentOrNull
                if (token == null) PocketBaseResult.Failure(PocketBaseError(message = "Missing token in response"))
                else {
                    client.authStore.save(token, res.value["record"]?.jsonObject)
                    PocketBaseResult.Success(RecordAuth(token))
                }
            }
            is PocketBaseResult.Failure -> res
        }
    }

    override suspend fun requestPasswordReset(collectionIdOrName: String, email: String): PocketBaseResult<Unit> =
        unit(client.post("/api/collections/$collectionIdOrName/request-password-reset", buildJsonObject { put("email", email) }))

    override suspend fun confirmPasswordReset(
        collectionIdOrName: String,
        token: String,
        password: String,
        passwordConfirm: String,
    ): PocketBaseResult<Unit> = unit(
        client.post(
            "/api/collections/$collectionIdOrName/confirm-password-reset",
            buildJsonObject {
                put("token", token)
                put("password", password)
                put("passwordConfirm", passwordConfirm)
            },
        ),
    )

    override suspend fun requestVerification(collectionIdOrName: String, email: String): PocketBaseResult<Unit> =
        unit(client.post("/api/collections/$collectionIdOrName/request-verification", buildJsonObject { put("email", email) }))

    override suspend fun confirmVerification(collectionIdOrName: String, token: String): PocketBaseResult<Unit> =
        unit(client.post("/api/collections/$collectionIdOrName/confirm-verification", buildJsonObject { put("token", token) }))

    override suspend fun requestEmailChange(collectionIdOrName: String, newEmail: String): PocketBaseResult<Unit> =
        unit(client.post("/api/collections/$collectionIdOrName/request-email-change", buildJsonObject { put("newEmail", newEmail) }))

    override suspend fun confirmEmailChange(collectionIdOrName: String, token: String, password: String): PocketBaseResult<Unit> =
        unit(
            client.post(
                "/api/collections/$collectionIdOrName/confirm-email-change",
                buildJsonObject {
                    put("token", token)
                    put("password", password)
                },
            ),
        )

    override suspend fun requestOtp(
        collectionIdOrName: String,
        email: String,
    ): PocketBaseResult<kotlinx.serialization.json.JsonObject> =
        client.post("/api/collections/$collectionIdOrName/request-otp", buildJsonObject { put("email", email) })

    override suspend fun authWithOtp(
        collectionIdOrName: String,
        otpId: String,
        password: String,
        expand: String?,
        fields: String?,
    ): PocketBaseResult<RecordAuth> {
        val res = client.post(
            pathWithQuery(
                "/api/collections/$collectionIdOrName/auth-with-otp",
                "expand" to expand,
                "fields" to fields,
            ),
            buildJsonObject {
                put("otpId", otpId)
                put("password", password)
            },
        )
        return when (res) {
            is PocketBaseResult.Success -> {
                val token = res.value["token"]?.jsonPrimitive?.contentOrNull
                if (token == null) PocketBaseResult.Failure(PocketBaseError(message = "Missing token in response"))
                else {
                    client.authStore.save(token, res.value["record"]?.jsonObject)
                    PocketBaseResult.Success(RecordAuth(token))
                }
            }
            is PocketBaseResult.Failure -> res
        }
    }

    override suspend fun impersonate(
        collectionIdOrName: String,
        recordId: String,
        duration: Int?,
        expand: String?,
        fields: String?,
    ): PocketBaseResult<RecordAuth> {
        val res = client.post(
            pathWithQuery(
                "/api/collections/$collectionIdOrName/impersonate/$recordId",
                "expand" to expand,
                "fields" to fields,
            ),
            duration?.let { buildJsonObject { put("duration", it) } },
        )
        return when (res) {
            is PocketBaseResult.Success -> {
                val token = res.value["token"]?.jsonPrimitive?.contentOrNull
                if (token == null) PocketBaseResult.Failure(PocketBaseError(message = "Missing token in response"))
                else PocketBaseResult.Success(RecordAuth(token))
            }
            is PocketBaseResult.Failure -> res
        }
    }

    override fun clear() {
        client.authStore.clear()
    }

    private fun unit(result: PocketBaseResult<*>): PocketBaseResult<Unit> = when (result) {
        is PocketBaseResult.Success -> PocketBaseResult.Success(Unit)
        is PocketBaseResult.Failure -> result
    }

    private fun pathWithQuery(path: String, vararg params: Pair<String, String?>): String {
        val encoded = params
            .filter { it.second != null }
            .joinToString("&") { "${it.first}=${it.second}" }
        return if (encoded.isBlank()) path else "$path?$encoded"
    }
}
