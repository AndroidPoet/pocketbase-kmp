package io.github.androidpoet.pocketbase.realtime

import io.github.androidpoet.pocketbase.client.PocketBaseClient
import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

public class RealtimeClientImpl(
    private val client: PocketBaseClient,
    private val httpClient: HttpClient = HttpClient(),
    private val maxRetryAttempts: Int = Int.MAX_VALUE,
    private val enableStreaming: Boolean = true,
) : RealtimeClient {
    private var clientId: String = ""
    private val subscriptions: MutableSet<String> = linkedSetOf()
    private var listener: ((event: String, data: String) -> Unit)? = null
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var streamJob: Job? = null
    private var closed: Boolean = false

    override suspend fun connect(): PocketBaseResult<String> {
        if (clientId.isNotBlank()) return PocketBaseResult.Success(clientId)
        return when (val res = client.get("/api/realtime")) {
            is PocketBaseResult.Failure -> res
            is PocketBaseResult.Success -> {
                val id = res.value["clientId"]?.jsonPrimitive?.contentOrNull
                if (id.isNullOrBlank()) {
                    PocketBaseResult.Failure(PocketBaseError(message = "Missing realtime clientId"))
                } else {
                    clientId = id
                    if (enableStreaming) startStream()
                    PocketBaseResult.Success(id)
                }
            }
        }
    }

    override suspend fun subscribe(topic: String): PocketBaseResult<Unit> {
        val conn = connect()
        if (conn is PocketBaseResult.Failure) return conn
        subscriptions += topic
        return submitSubscriptions()
    }

    override suspend fun unsubscribe(topic: String): PocketBaseResult<Unit> {
        subscriptions.remove(topic)
        return submitSubscriptions()
    }

    override suspend fun unsubscribeAll(): PocketBaseResult<Unit> {
        subscriptions.clear()
        return submitSubscriptions()
    }

    override fun onEvent(listener: (event: String, data: String) -> Unit) {
        this.listener = listener
    }

    override fun close() {
        closed = true
        streamJob?.cancel()
        streamJob = null
        scope.cancel()
    }

    private suspend fun submitSubscriptions(): PocketBaseResult<Unit> {
        if (clientId.isBlank()) return PocketBaseResult.Success(Unit)
        val body = buildJsonObject {
            put("clientId", clientId)
            put("subscriptions", buildJsonArray { subscriptions.forEach { add(JsonPrimitive(it)) } })
        }
        val result = client.post("/api/realtime", body)
        return when (result) {
            is PocketBaseResult.Success -> PocketBaseResult.Success(Unit)
            is PocketBaseResult.Failure -> result
        }
    }

    private fun startStream() {
        if (streamJob != null || closed) return
        val url = client.buildUrl("/api/realtime")
        streamJob = scope.launch {
            var attempts = 0
            while (!closed) {
                try {
                    httpClient.prepareGet(url) {
                        if (client.authStore.isValid) header(HttpHeaders.Authorization, client.authStore.token)
                    }.execute { response ->
                        attempts = 0
                        val channel = response.bodyAsChannel()
                        var event = "message"
                        while (!channel.isClosedForRead && !closed) {
                            val line = channel.readUTF8Line() ?: break
                            if (line.isBlank()) continue
                            if (line.startsWith("event:")) {
                                event = line.removePrefix("event:").trim()
                                continue
                            }
                            if (line.startsWith("data:")) {
                                val data = line.removePrefix("data:").trim()
                                listener?.invoke(event, data)
                            }
                        }
                    }
                    if (!closed) {
                        attempts += 1
                        if (attempts > maxRetryAttempts) break
                        reconnect(attempts)
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    attempts += 1
                    if (attempts > maxRetryAttempts || closed) break
                    reconnect(attempts)
                }
            }
        }
    }

    private suspend fun reconnect(attempt: Int) {
        delay(nextRetryDelayMs(attempt))
        clientId = ""
        when (val connected = connect()) {
            is PocketBaseResult.Success -> submitSubscriptions()
            is PocketBaseResult.Failure -> {}
        }
    }

    internal fun nextRetryDelayMs(attempt: Int): Long {
        val timeouts = longArrayOf(200, 300, 500, 1000, 1200, 1500, 2000)
        return if (attempt <= 0) timeouts[0] else timeouts[minOf(attempt - 1, timeouts.lastIndex)]
    }
}
