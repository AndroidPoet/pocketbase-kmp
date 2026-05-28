package io.github.androidpoet.pocketbase.core.result

public sealed interface PocketBaseResult<out T> {
    public data class Success<T>(val value: T) : PocketBaseResult<T>
    public data class Failure(val error: PocketBaseError) : PocketBaseResult<Nothing>
}

public inline fun <T> pocketbaseResult(block: () -> T): PocketBaseResult<T> =
    try {
        PocketBaseResult.Success(block())
    } catch (t: Throwable) {
        PocketBaseResult.Failure(PocketBaseError(message = t.message ?: "Unknown error", cause = t))
    }
