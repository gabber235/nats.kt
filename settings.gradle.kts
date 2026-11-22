rootProject.name = "natskt"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include("architecture")
include("integration-tests")
include("test-harness")
include("test-harness:nats-server-daemon")
include("test-harness:tls-test-server")

/*
 * Library projects are named after their published artifactId. Gradle source dependencies
 * substitute an external module by the project's group and name, not by the publication
 * coordinates, so `io.github.n-hass:natskt-core` resolves to the project named natskt-core.
 */
fun includeLibrary(artifactId: String, directory: String) {
	include(artifactId)
	project(":$artifactId").projectDir = file(directory)
}

includeLibrary("natskt-core", "core")
includeLibrary("natskt-core-common", "core/common")
includeLibrary("natskt-transport-tcp", "core/transport-tcp")
includeLibrary("natskt-transport-ws", "core/transport-ws")
includeLibrary("natskt-crypto", "crypto")
includeLibrary("natskt-crypto-headless", "crypto-headless")
includeLibrary("natskt-internal", "internal")
includeLibrary("natskt-jetstream", "jetstream")
includeLibrary("natskt-nkeys", "nkeys")
includeLibrary("natskt-nuid", "nuid")
includeLibrary("natskt-platform", "platform")
