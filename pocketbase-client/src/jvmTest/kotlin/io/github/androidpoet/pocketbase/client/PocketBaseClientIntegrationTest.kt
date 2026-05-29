package io.github.androidpoet.pocketbase.client

import io.github.androidpoet.pocketbase.core.result.PocketBaseResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PocketBaseClientIntegrationTest {
    @Test
    fun test_health_check_against_docker_instance_returns_success() = runBlocking {
        val baseUrl = System.getenv("POCKETBASE_BASE_URL") ?: return@runBlocking
        val client = PocketBase.create(baseUrl).client
        val healthService = HealthService(client)

        val result = healthService.check()

        assertTrue(result is PocketBaseResult.Success)
        assertEquals(200, result.value["code"]?.toString()?.trim('"')?.toIntOrNull())
    }
}
