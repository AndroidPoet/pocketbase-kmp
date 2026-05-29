plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}

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
