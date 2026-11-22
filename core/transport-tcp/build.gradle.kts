plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    explicitApi()

    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.natsktCoreCommon)
                implementation(libs.whyoleg.secureRandom)
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.ktor.io)
                implementation(libs.ktor.network)
                implementation(libs.ktor.network.tls)
            }
        }
    }
}

mavenPublishing {
	coordinates(artifactId = "natskt-transport-tcp")
	publishToMavenCentral()

	pom {
		name = "NATS Kotlin Client - TCP transport"
		description = "Part of natskt-core"
	}
}
