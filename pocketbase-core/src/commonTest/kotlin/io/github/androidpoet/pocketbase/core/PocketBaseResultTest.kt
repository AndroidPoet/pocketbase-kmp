package io.github.androidpoet.pocketbase.core

import io.github.androidpoet.pocketbase.core.result.PocketBaseError
import io.github.androidpoet.pocketbase.core.result.PocketBaseException
import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import io.github.androidpoet.pocketbase.core.result.toPocketBaseResult
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PocketBaseResultTest {
    @Test
    fun test_catching_rethrowsCancellationException() {
        assertFailsWith<CancellationException> {
            PocketBaseResult.catching<Int> {
                throw CancellationException("cancel")
            }
        }
    }

    @Test
    fun test_toPocketBaseResult_rethrowsCancellationException() {
        assertFailsWith<CancellationException> {
            Result.failure<String>(CancellationException("stop")).toPocketBaseResult()
        }
    }

    @Test
    fun test_getOrThrow_throwsPocketBaseExceptionForFailure() {
        val error = PocketBaseError(message = "boom")
        val result: PocketBaseResult<String> = PocketBaseResult.Failure(error)

        val thrown = assertFailsWith<PocketBaseException> { result.getOrThrow() }
        assertEquals(error, thrown.error)
    }

    @Test
    fun test_catching_wrapsPocketBaseExceptionAsFailure() {
        val error = PocketBaseError(message = "mapped")

        val result = PocketBaseResult.catching<String> {
            throw PocketBaseException(error)
        }

        assertTrue(result.isFailure)
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun test_toPocketBaseResult_mapsPocketBaseException() {
        val error = PocketBaseError(message = "oops")
        val result = Result.failure<String>(PocketBaseException(error)).toPocketBaseResult()
        val failure = assertIs<PocketBaseResult.Failure>(result)
        assertEquals(error, failure.error)
    }
}
