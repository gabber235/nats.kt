plugins {
	alias(libs.plugins.natskt.kmp)
}

kotlin {
	explicitApi()
	allTargets()

	sourceSets {
		commonMain.dependencies {
			implementation(libs.whyoleg.cryptography.core)
		}
		jvmMain.dependencies {
			api(libs.whyoleg.cryptography.provider.jdk.bc)
		}
	}
}

mavenPublishing {
	coordinates(artifactId = "natskt-crypto-headless")
	publishToMavenCentral()

	pom {
		name = "NATS Kotlin Client - Cryptography Providers"
		description = "Part of natskt-core"
	}
}
