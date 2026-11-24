package io.natskt.jetstream.integration

import harness.RemoteNatsHarness
import harness.RemoteNatsServer
import harness.runBlocking
import harness.waitForLog
import io.natskt.NatsClient
import io.natskt.jetstream.JetStreamClient
import io.natskt.jetstream.api.JetStreamApiException
import io.natskt.jetstream.api.PublishOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * A JetStream publish sends its message again only when no stream was there to take it (no responders). Any other
 * answer is the stream's verdict, and a request that timed out or was cancelled may have landed, so sending it again
 * would turn a landed conditional write into a wrong last sequence error and a plain one into a duplicate.
 */
class PublishRetryTest {
	@Test
	fun `a publish answered with a wrong last sequence fails after one request`() =
		RemoteNatsHarness.runBlocking { server ->
			withJetStreamClient(server) { _, js ->
				js.manager.createStream {
					name = "publish_retry_conflict"
					subject("retry.conflict.>")
				}
				val subject = "retry.conflict.key"
				val logsBefore = server.logSnapshot().size

				val failure =
					runCatching {
						js.publish(
							subject,
							"stale".encodeToByteArray(),
							publishOptions = PublishOptions(expectedLastSubjectSequence = 99u),
						)
					}.exceptionOrNull()

				assertIs<JetStreamApiException>(failure)
				assertEquals(WRONG_LAST_SEQUENCE, failure.error?.errCode)
				assertEquals(1, server.publishesTo(subject, after = logsBefore))
			}
		}

	@Test
	fun `a publish no stream answers is sent again until its retries run out`() =
		RemoteNatsHarness.runBlocking { server ->
			withJetStreamClient(server) { _, js ->
				val subject = "retry.nostream.key"
				val logsBefore = server.logSnapshot().size

				val failure =
					runCatching {
						js.publish(
							subject,
							"lost".encodeToByteArray(),
							publishOptions = PublishOptions(retryAttempts = 2, retryWait = 10.milliseconds),
						)
					}.exceptionOrNull()

				assertIs<JetStreamApiException>(failure)
				assertEquals(NO_RESPONDERS, failure.error?.code)
				assertEquals(3, server.publishesTo(subject, after = logsBefore))
			}
		}

	@Test
	fun `a publish whose answer never came is not sent again`() =
		RemoteNatsHarness.runBlocking { server ->
			val client =
				NatsClient {
					this.server = server.uri
					noResponders = false
				}
			client.connect().getOrThrow()
			try {
				val js = JetStreamClient(client)
				val subject = "retry.unanswered.key"
				val logsBefore = server.logSnapshot().size

				val failure =
					withTimeout(30.seconds) {
						runCatching { js.publish(subject, "unanswered".encodeToByteArray()) }.exceptionOrNull()
					}

				assertIs<CancellationException>(failure, "a timed out request surfaces as natskt's request timeout")
				assertEquals(1, server.publishesTo(subject, after = logsBefore))
			} finally {
				client.disconnect()
			}
		}

	@Test
	fun `a cancelled publish sends nothing more`() =
		RemoteNatsHarness.runBlocking { server ->
			val client =
				NatsClient {
					this.server = server.uri
					noResponders = false
				}
			client.connect().getOrThrow()
			try {
				val js = JetStreamClient(client)
				val subject = "retry.cancelled.key"
				val logsBefore = server.logSnapshot().size
				val publishing =
					launch(start = CoroutineStart.UNDISPATCHED) {
						js.publish(subject, "cancelled".encodeToByteArray())
					}
				waitForLog(server) { it.contains("PUB $subject ") }

				publishing.cancel()
				withTimeout(1.seconds) { publishing.join() }
				// Longer than the old loop took to send its two retries, which followed at once.
				delay(1.seconds)

				assertTrue(publishing.isCancelled)
				assertEquals(1, server.publishesTo(subject, after = logsBefore))
			} finally {
				client.disconnect()
			}
		}

	private suspend fun RemoteNatsServer.publishesTo(
		subject: String,
		after: Int,
	): Int {
		// The server writes its trace lines shortly after it handles them.
		delay(200.milliseconds)
		return logSnapshot().drop(after).count { it.contains("PUB $subject ") }
	}

	private companion object {
		const val WRONG_LAST_SEQUENCE = 10071
		const val NO_RESPONDERS = 503
	}
}
