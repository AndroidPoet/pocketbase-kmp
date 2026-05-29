plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}

val integrationContainerName = "pocketbase-kmp-it"
val integrationPort = "8090"
val integrationImage = "ghcr.io/muchobien/pocketbase:latest"

tasks.register("checkJvm") {
    group = "verification"
    description = "Runs JVM compile and tests for all modules without Android tasks."

    dependsOn(
        ":pocketbase-core:compileKotlinJvm",
        ":pocketbase-client:compileKotlinJvm",
        ":pocketbase-auth:compileKotlinJvm",
        ":pocketbase-records:compileKotlinJvm",
        ":pocketbase-files:compileKotlinJvm",
        ":pocketbase-realtime:compileKotlinJvm",
        ":pocketbase-core:jvmTest",
        ":pocketbase-client:jvmTest",
        ":pocketbase-auth:jvmTest",
        ":pocketbase-records:jvmTest",
        ":pocketbase-files:jvmTest",
        ":pocketbase-realtime:jvmTest",
    )
}

tasks.register<Exec>("integrationDockerStart") {
    group = "verification"
    description = "Starts a local PocketBase Docker container for integration tests."
    commandLine(
        "sh",
        "-c",
        """
        docker rm -f $integrationContainerName >/dev/null 2>&1 || true
        docker run -d --name $integrationContainerName -p $integrationPort:8090 $integrationImage serve --http=0.0.0.0:8090
        """.trimIndent(),
    )
}

tasks.register<Exec>("integrationDockerWait") {
    group = "verification"
    description = "Waits until PocketBase health endpoint responds."
    commandLine(
        "sh",
        "-c",
        """
        for i in $(seq 1 60); do
          if curl -fsS http://127.0.0.1:$integrationPort/api/health >/dev/null; then
            exit 0
          fi
          sleep 1
        done
        echo "PocketBase container did not become healthy in time."
        exit 1
        """.trimIndent(),
    )
    dependsOn("integrationDockerStart")
}

tasks.register<Exec>("integrationDockerStop") {
    group = "verification"
    description = "Stops and removes the PocketBase integration test container."
    isIgnoreExitValue = true
    commandLine("sh", "-c", "docker rm -f $integrationContainerName >/dev/null 2>&1 || true")
}

tasks.register<Exec>("integrationTestDocker") {
    group = "verification"
    description = "Runs PocketBase SDK integration tests against a Docker PocketBase instance."
    dependsOn("integrationDockerWait")
    finalizedBy("integrationDockerStop")
    environment("POCKETBASE_BASE_URL", "http://127.0.0.1:$integrationPort")
    commandLine(
        "./gradlew",
        ":pocketbase-client:jvmTest",
        "--tests",
        "io.github.androidpoet.pocketbase.client.PocketBaseClientIntegrationTest",
        "--no-configuration-cache",
    )
}
