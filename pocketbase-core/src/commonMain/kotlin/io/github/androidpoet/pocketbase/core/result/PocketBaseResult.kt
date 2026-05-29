package io.github.androidpoet.pocketbase.core.result

import kotlinx.coroutines.CancellationException

public sealed interface PocketBaseResult<out T> {
    public data class Success<out T>(public val value: T) : PocketBaseResult<T>
    public data class Failure(public val error: PocketBaseError) : PocketBaseResult<Nothing>
    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is Failure

    public fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    public fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error.toException()
    }

    public fun errorOrNull(): PocketBaseError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    public companion object {
        public inline fun <T> catching(block: () -> T): PocketBaseResult<T> =
            try {
                Success(block())
            } catch (e: PocketBaseException) {
                Failure(e.error)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Failure(PocketBaseError(message = t.message ?: "Unknown error", cause = t))
            }
    }
}

public inline fun <T> pocketbaseResult(block: () -> T): PocketBaseResult<T> =
    PocketBaseResult.catching(block)

public inline fun <T, R> PocketBaseResult<T>.map(
    transform: (T) -> R,
): PocketBaseResult<R> = when (this) {
    is PocketBaseResult.Success -> PocketBaseResult.Success(transform(value))
    is PocketBaseResult.Failure -> this
}

public inline fun <T, R> PocketBaseResult<T>.flatMap(
    transform: (T) -> PocketBaseResult<R>,
): PocketBaseResult<R> = when (this) {
    is PocketBaseResult.Success -> transform(value)
    is PocketBaseResult.Failure -> this
}

public inline fun <T> PocketBaseResult<T>.onSuccess(
    action: (T) -> Unit,
): PocketBaseResult<T> = apply {
    if (this is PocketBaseResult.Success) action(value)
}

public inline fun <T> PocketBaseResult<T>.onFailure(
    action: (PocketBaseError) -> Unit,
): PocketBaseResult<T> = apply {
    if (this is PocketBaseResult.Failure) action(error)
}

public inline fun <T> PocketBaseResult<T>.recover(
    transform: (PocketBaseError) -> T,
): PocketBaseResult<T> = when (this) {
    is PocketBaseResult.Success -> this
    is PocketBaseResult.Failure -> PocketBaseResult.Success(transform(error))
}

public inline fun <T> PocketBaseResult<T>.getOrElse(
    defaultValue: (PocketBaseError) -> T,
): T = when (this) {
    is PocketBaseResult.Success -> value
    is PocketBaseResult.Failure -> defaultValue(error)
}

public fun <T> PocketBaseResult<T>.toKotlinResult(): Result<T> = when (this) {
    is PocketBaseResult.Success -> Result.success(value)
    is PocketBaseResult.Failure -> Result.failure(error.toException())
}

public inline fun <T> Result<T>.toPocketBaseResult(
    mapThrowable: (Throwable) -> PocketBaseError = { throwable ->
        val pocketBaseException = throwable as? PocketBaseException
        pocketBaseException?.error ?: PocketBaseError(message = throwable.message ?: "Unknown error", cause = throwable)
    },
): PocketBaseResult<T> = fold(
    onSuccess = { PocketBaseResult.Success(it) },
    onFailure = { throwable ->
        if (throwable is CancellationException) throw throwable
        PocketBaseResult.Failure(mapThrowable(throwable))
    },
)
