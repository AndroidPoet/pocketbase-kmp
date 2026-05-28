package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

public class HealthService(
    private val client: PocketBaseClient,
) {
    public suspend fun check(): PocketBaseResult<JsonObject> = client.get("/api/health")
}

public class CollectionsService(
    private val client: PocketBaseClient,
) {
    public suspend fun list(page: Int = 1, perPage: Int = 30): PocketBaseResult<JsonObject> =
        client.get("/api/collections", mapOf("page" to page.toString(), "perPage" to perPage.toString()))

    public suspend fun getOne(idOrName: String): PocketBaseResult<JsonObject> = client.get("/api/collections/$idOrName")
    public suspend fun create(body: JsonObject): PocketBaseResult<JsonObject> = client.post("/api/collections", body)
    public suspend fun update(idOrName: String, body: JsonObject): PocketBaseResult<JsonObject> = client.patch("/api/collections/$idOrName", body)
    public suspend fun delete(idOrName: String): PocketBaseResult<JsonObject> = client.delete("/api/collections/$idOrName")
    public suspend fun truncate(idOrName: String): PocketBaseResult<JsonObject> = client.delete("/api/collections/$idOrName/truncate")
}

public class BatchService(
    private val client: PocketBaseClient,
) {
    private val requests = mutableListOf<JsonObject>()
    private val files = mutableListOf<MultipartFilePart>()

    public fun create(
        collection: String,
        body: JsonObject,
        fileParts: List<MultipartFilePart> = emptyList(),
    ): BatchService = apply {
        val requestIndex = requests.size
        requests += buildJsonObject {
            put("method", "POST")
            put("url", "/api/collections/$collection/records")
            put("body", body)
        }
        fileParts.forEach { file ->
            files += file.copy(field = "requests.$requestIndex.${file.field}")
        }
    }

    public fun update(
        collection: String,
        id: String,
        body: JsonObject,
        fileParts: List<MultipartFilePart> = emptyList(),
    ): BatchService = apply {
        val requestIndex = requests.size
        requests += buildJsonObject {
            put("method", "PATCH")
            put("url", "/api/collections/$collection/records/$id")
            put("body", body)
        }
        fileParts.forEach { file ->
            files += file.copy(field = "requests.$requestIndex.${file.field}")
        }
    }

    public fun delete(collection: String, id: String): BatchService = apply {
        requests += buildJsonObject {
            put("method", "DELETE")
            put("url", "/api/collections/$collection/records/$id")
        }
    }

    public suspend fun send(): PocketBaseResult<JsonObject> {
        val payload = buildJsonObject {
            put("requests", buildJsonArray { requests.forEach { add(it) } })
        }
        return if (files.isEmpty()) {
            client.post("/api/batch", payload)
        } else {
            client.postMultipart(
                "/api/batch",
                fields = mapOf("requests" to payload["requests"].toString()),
                files = files.toList(),
            )
        }
    }
}

public class BackupsService(
    private val client: PocketBaseClient,
) {
    public suspend fun list(): PocketBaseResult<JsonObject> = client.get("/api/backups")
    public suspend fun create(name: String? = null): PocketBaseResult<JsonObject> =
        client.post("/api/backups", body = name?.let { buildJsonObject { put("name", it) } })
    public suspend fun restore(key: String): PocketBaseResult<JsonObject> = client.post("/api/backups/$key/restore")
    public suspend fun delete(key: String): PocketBaseResult<JsonObject> = client.delete("/api/backups/$key")
}

public class LogsService(
    private val client: PocketBaseClient,
) {
    public suspend fun list(page: Int = 1, perPage: Int = 30): PocketBaseResult<JsonObject> =
        client.get("/api/logs", mapOf("page" to page.toString(), "perPage" to perPage.toString()))
    public suspend fun getOne(id: String): PocketBaseResult<JsonObject> = client.get("/api/logs/$id")
    public suspend fun stats(days: Int = 30): PocketBaseResult<JsonObject> = client.get("/api/logs/stats", mapOf("days" to days.toString()))
}

public class SettingsService(
    private val client: PocketBaseClient,
) {
    public suspend fun getAll(): PocketBaseResult<JsonObject> = client.get("/api/settings")
    public suspend fun update(body: JsonObject): PocketBaseResult<JsonObject> = client.patch("/api/settings", body)
    public suspend fun testS3(body: JsonObject): PocketBaseResult<JsonObject> = client.post("/api/settings/test/s3", body)
    public suspend fun testEmail(body: JsonObject): PocketBaseResult<JsonObject> = client.post("/api/settings/test/email", body)
}

public class CronsService(
    private val client: PocketBaseClient,
) {
    public suspend fun list(): PocketBaseResult<JsonObject> = client.get("/api/crons")
    public suspend fun run(jobId: String): PocketBaseResult<JsonObject> = client.post("/api/crons/$jobId")
}
