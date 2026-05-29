package io.github.androidpoet.pocketbase.files

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.Serializable

@Serializable
public data class FileToken(val token: String)

public interface FilesClient {
    public fun getUrl(
        collectionIdOrName: String,
        recordId: String,
        filename: String,
        thumb: String? = null,
        token: String? = null,
        download: Boolean? = null,
    ): String
    public suspend fun getToken(): PocketBaseResult<FileToken>
}
