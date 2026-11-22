plugins {
	alias(libs.plugins.natskt.kmp)
}

kotlin {
	explicitApi()
	allTargets()

	sourceSets {
		commonMain.dependencies {
			api(projects.natsktCore)
			api(projects.natsktJetstream)
			implementation(projects.natsktCrypto)
		}
	}
}

mavenPublishing {
	coordinates(artifactId = "natskt-platform")
	publishToMavenCentral()

	pom {
		name = "NATS Kotlin Client - Platform Bundle"
		description = "Full set of core, jetstream and cryptography libraries for NATS.kt"
	}
}
