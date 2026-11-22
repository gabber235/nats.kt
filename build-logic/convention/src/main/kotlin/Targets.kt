import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Standard nats.kt target set for the Typewriter fork: `jvm` only.
 *
 * The fork is consumed as a Gradle source dependency, so every target declared here is
 * configured inside the consumer's build. Keeping only the JVM target avoids JS, Wasm and
 * Kotlin/Native toolchains there.
 */
fun KotlinMultiplatformExtension.allTargets() {
	jvm()
}
