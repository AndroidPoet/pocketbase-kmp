package io.github.androidpoet.pocketbase.core.result

public class PocketBaseException(
    public val error: PocketBaseError,
) : RuntimeException(error.message, error.cause)

public fun PocketBaseError.toException(): PocketBaseException = PocketBaseException(this)
