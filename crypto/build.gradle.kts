plugins {
	alias(libs.plugins.natskt.kmp)
}

kotlin {
	explicitApi()
	allTargets()

	sourceSets {
		commonMain.dependencies {
			implementation(libs.whyoleg.cryptography.core)
			api(projects.natsktCryptoHeadless)
		}
	}
}

mavenPublishing {
	coordinates(artifactId = "natskt-crypto")
	publishToMavenCentral()

	pom {
		name = "NATS Kotlin Client - Cryptography Providers"
		description = "Part of natskt-core"
	}
}
