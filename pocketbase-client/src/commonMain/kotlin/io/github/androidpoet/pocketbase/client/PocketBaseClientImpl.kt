package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonObject

public class PocketBaseClientImpl(
    override val config: PocketBaseConfig,
    override val authStore: AuthStore = AuthStore(),
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) { json(config.json) }
    },
) : PocketBaseClient {
    override suspend fun get(path: String, query: Map<String, String?>): PocketBaseResult<JsonObject> = request {
        httpClient.get(buildUrl(path, query)) { applyHeaders() }.body()
    }

    override suspend fun post(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = request {
        httpClient.post(buildUrl(path)) {
            applyHeaders()
            contentType(ContentType.Application.Json)
            if (body != null) setBody(body)
        }.body()
    }

    override suspend fun patch(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = request {
        httpClient.patch(buildUrl(path)) {
            applyHeaders()
            contentType(ContentType.Application.Json)
            if (body != null) setBody(body)
        }.body()
    }

    override suspend fun put(path: String, body: JsonObject?): PocketBaseResult<JsonObject> = request {
        httpClient.put(buildUrl(path)) {
            applyHeaders()
            contentType(ContentType.Application.Json)
            if (body != null) setBody(body)
        }.body()
    }

    override suspend fun delete(path: String): PocketBaseResult<JsonObject> = request {
        httpClient.delete(buildUrl(path)) { applyHeaders() }.body()
    }

    override suspend fun postMultipart(
        path: String,
        fields: Map<String, String>,
        files: List<MultipartFilePart>,
    ): PocketBaseResult<JsonObject> = request {
        httpClient.post(buildUrl(path)) {
            applyHeaders()
            setBody(
                MultiPartFormDataContent(
                    formData {
                        fields.forEach { (k, v) -> append(k, v) }
                        files.forEach { file ->
                            append(
                                key = file.field,
                                value = file.bytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, file.contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
                                },
                            )
                        }
                    },
                ),
            )
        }.body()
    }

    override fun buildUrl(path: String, query: Map<String, String?>): String {
        val normalized = "${config.baseUrl.trimEnd('/')}/${path.trimStart('/')}"
        if (query.isEmpty()) return normalized
        val parts = query.entries.filter { it.value != null }.joinToString("&") { "${it.key}=${it.value}" }
        return if (parts.isBlank()) normalized else "$normalized?$parts"
    }

    private inline fun <T> request(block: () -> T): PocketBaseResult<T> =
        try {
            PocketBaseResult.Success(block())
        } catch (t: Throwable) {
            PocketBaseResult.Failure(PocketBaseError(message = t.message ?: "Request failed", cause = t))
        }

    private fun HttpRequestBuilder.applyHeaders() {
        if (authStore.isValid) headers.append(HttpHeaders.Authorization, authStore.token)
        config.lang?.let { headers.append(HttpHeaders.AcceptLanguage, it) }
    }
}
