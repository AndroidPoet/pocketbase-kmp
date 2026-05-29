package io.github.androidpoet.pocketbase.files

import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

public class FilesClientImpl(
    private val client: PocketBaseClient,
) : FilesClient {
    override fun getUrl(
        collectionIdOrName: String,
        recordId: String,
        filename: String,
        thumb: String?,
        token: String?,
        download: Boolean?,
    ): String =
        client.buildUrl(
            "/api/files/$collectionIdOrName/$recordId/$filename",
            mapOf(
                "thumb" to thumb,
                "token" to token,
                "download" to download?.toString(),
            ),
        )

    override suspend fun getToken(): PocketBaseResult<FileToken> =
        when (val res = client.post("/api/files/token")) {
            is PocketBaseResult.Success -> {
                val token = res.value["token"]?.jsonPrimitive?.contentOrNull
                if (token == null) PocketBaseResult.Failure(PocketBaseError(message = "Missing token in response"))
                else PocketBaseResult.Success(FileToken(token))
            }
            is PocketBaseResult.Failure -> res
        }
}
