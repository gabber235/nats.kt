plugins {
	alias(libs.plugins.natskt.kmp)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()
    allTargets()

    sourceSets {
        commonMain.dependencies {
			implementation(projects.natsktCore)
			implementation(projects.natsktCoreCommon)
			implementation(projects.natsktInternal)
			implementation(libs.whyoleg.cryptography.core)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.kotlinx.serialization.core)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.ktor.http)
		}

		commonTest.dependencies {
			implementation(kotlin("test"))
			implementation(libs.kotlinx.coroutines.test)
			implementation(libs.kotest.assertions.core)
			implementation(libs.turbine)
			implementation(projects.testHarness)
			implementation(projects.natsktCrypto)
		}

		jvmTest.dependencies {
			implementation(projects.testHarness)
			implementation("org.slf4j:slf4j-simple:2.0.17")
			implementation("io.nats:jnats:2.22.0")
		}
    }
}

mavenPublishing {
	coordinates(artifactId = "natskt-jetstream")
	publishToMavenCentral()

	pom {
		name = "NATS Kotlin Client - JetStream"
		description = "JetStream extensions from NATS.kt core"
	}
}
