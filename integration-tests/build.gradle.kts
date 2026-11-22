plugins {
	kotlin("multiplatform")
}

kotlin {
	applyDefaultHierarchyTemplate()

	jvm()

	sourceSets {
		val commonTest by getting

		val nativeAndJvmSharedTest by creating {
			dependsOn(commonTest)
		}

		jvmTest { dependsOn(nativeAndJvmSharedTest) }

		commonTest.dependencies {
			implementation(kotlin("test"))
			implementation(projects.natsktCore)
			implementation(projects.testHarness)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.kotlinx.coroutines.test)
			implementation(libs.ktor.client.engine.cio)
		}

		jvmTest.dependencies {
			implementation(libs.ktor.client.engine.java)
			implementation(libs.ktor.client.engine.okhttp)
		}
	}
}
