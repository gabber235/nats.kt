plugins {
	alias(libs.plugins.natskt.kmp)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	explicitApi()
	allTargets()

	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.kotlinx.coroutines.test)
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.serialization.kotlinx.json)
			implementation(projects.natsktCore)
		}

		commonTest.dependencies {
			implementation(kotlin("test"))
		}

		jvmMain.dependencies {
			implementation(libs.ktor.client.engine.cio)
		}
	}
}
