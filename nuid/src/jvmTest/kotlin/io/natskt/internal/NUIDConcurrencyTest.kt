package io.natskt.internal

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class NUIDConcurrencyTest {
	@Test
	fun `next returns unique values when called from many threads`() {
		val nuid = NUID()

		val values = generateConcurrently { nuid.next() }

		assertEquals(THREADS * CALLS_PER_THREAD, values.size, "concurrent next() calls returned duplicate values")
	}

	@Test
	fun `nextSequence returns unique values when called from many threads`() {
		val nuid = NUID()

		val values = generateConcurrently { nuid.nextSequence() }

		assertEquals(THREADS * CALLS_PER_THREAD, values.size, "concurrent nextSequence() calls returned duplicate values")
	}

	private fun generateConcurrently(generate: () -> String): Set<String> {
		val values = ConcurrentHashMap.newKeySet<String>()
		val start = CountDownLatch(1)
		val threads =
			List(THREADS) {
				thread {
					start.await()
					repeat(CALLS_PER_THREAD) { values.add(generate()) }
				}
			}
		start.countDown()
		threads.forEach { it.join() }
		return values
	}

	private companion object {
		const val THREADS = 16
		const val CALLS_PER_THREAD = 20_000
	}
}
