package io.github.androidpoet.pocketbase.client

import kotlinx.serialization.json.Json

public data class PocketBaseConfig(
    val baseUrl: String,
    val lang: String? = null,
    val json: Json = Json { ignoreUnknownKeys = true },
)
