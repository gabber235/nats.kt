plugins {
    alias(libs.plugins.natskt.kmp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()
    allTargets()

    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val nativeAndJvmSharedMain by creating {
            dependsOn(commonMain)
        }
        val nativeAndJvmSharedTest by creating {
            dependsOn(commonTest)
        }

        jvmMain { dependsOn(nativeAndJvmSharedMain) }
        jvmTest { dependsOn(nativeAndJvmSharedTest) }

        commonMain.dependencies {
            api(projects.natsktCoreCommon)
            api(projects.natsktTransportWs)
            api(projects.natsktNuid)
            implementation(projects.natsktInternal)
            implementation(projects.natsktNkeys)

            implementation(libs.whyoleg.secureRandom)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor.io)
            implementation(libs.ktor.network)
            implementation(libs.ktor.http)
        }

        nativeAndJvmSharedMain.dependencies {
            api(projects.natsktTransportTcp)
        }

		/* Tests */

		commonTest.dependencies {
			implementation(kotlin("test"))
			implementation(projects.testHarness)
			implementation(projects.natsktCrypto)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.kotlinx.coroutines.test)
			implementation(libs.turbine)
			implementation(libs.ktor.client.engine.cio)
			implementation(libs.whyoleg.cryptography.core)
		}

		jvmTest.dependencies {
			implementation(libs.ktor.client.engine.java)
			implementation(libs.ktor.client.engine.okhttp)
		}
    }
}

mavenPublishing {
	coordinates(artifactId = "natskt-core")
	publishToMavenCentral()

	pom {
		name = "NATS Kotlin Client"
		description = "A Kotlin Multiplatform client for NATS messaging system"
	}
}
