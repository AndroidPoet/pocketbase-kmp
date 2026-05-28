package io.github.androidpoet.pocketbase.core.result

public data class PocketBaseError(
    val statusCode: Int? = null,
    val message: String,
    val data: String? = null,
    val cause: Throwable? = null,
)
