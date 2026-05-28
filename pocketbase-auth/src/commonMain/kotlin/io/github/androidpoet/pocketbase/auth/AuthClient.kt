package io.github.androidpoet.pocketbase.auth

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.Serializable

@Serializable
public data class RecordAuth(
    val token: String,
)

public interface AuthClient {
    public suspend fun authMethods(collectionIdOrName: String): PocketBaseResult<kotlinx.serialization.json.JsonObject>
    public suspend fun authWithPassword(collectionIdOrName: String, identity: String, password: String): PocketBaseResult<RecordAuth>
    public suspend fun authWithOAuth2Code(
        collectionIdOrName: String,
        provider: String,
        code: String,
        codeVerifier: String? = null,
        redirectUrl: String? = null,
    ): PocketBaseResult<RecordAuth>
    public suspend fun refresh(collectionIdOrName: String): PocketBaseResult<RecordAuth>
    public suspend fun requestPasswordReset(collectionIdOrName: String, email: String): PocketBaseResult<Unit>
    public suspend fun confirmPasswordReset(
        collectionIdOrName: String,
        token: String,
        password: String,
        passwordConfirm: String,
    ): PocketBaseResult<Unit>
    public suspend fun requestVerification(collectionIdOrName: String, email: String): PocketBaseResult<Unit>
    public suspend fun confirmVerification(collectionIdOrName: String, token: String): PocketBaseResult<Unit>
    public suspend fun requestEmailChange(collectionIdOrName: String, newEmail: String): PocketBaseResult<Unit>
    public suspend fun confirmEmailChange(collectionIdOrName: String, token: String, password: String): PocketBaseResult<Unit>
    public suspend fun requestOtp(collectionIdOrName: String, email: String): PocketBaseResult<kotlinx.serialization.json.JsonObject>
    public suspend fun authWithOtp(collectionIdOrName: String, otpId: String, password: String): PocketBaseResult<RecordAuth>
    public suspend fun impersonate(collectionIdOrName: String, recordId: String, duration: Int? = null): PocketBaseResult<RecordAuth>
    public fun clear()
}
