package io.natskt.internal

import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private val DIGITS = ('0'..'9') + ('A'..'Z') + ('a'..'z')

private const val BASE = 62
private const val PRE_LEN = 12
private const val SEQ_LEN = 10
private const val MAX_SEQ = 839299365868340224L // base^seqLen == 62^10

private const val MIN_INC = 33L
private const val MAX_INC = 333L
private const val TOTAL_LEN = PRE_LEN + SEQ_LEN

/**
 * Generates unique identifiers for inboxes and consumer names.
 *
 * Safe to share between threads: every client in a process uses [Default], so each call
 * advances one immutable snapshot of prefix, sequence and increment with a compare and set,
 * and no two calls can observe the same sequence.
 */
@OptIn(ExperimentalAtomicApi::class)
public open class NUID {
	public companion object Default : NUID()

	private class State(
		val pre: CharArray,
		val seq: Long,
		val inc: Long,
	)

	private val state = AtomicReference(randomState())

	public val pre: CharArray get() = state.load().pre.copyOf()
	public val seq: Long get() = state.load().seq
	public val inc: Long get() = state.load().inc

	public fun next(): String {
		val current = advance()
		val b = CharArray(TOTAL_LEN)
		current.pre.copyInto(b, 0, 0, PRE_LEN)
		writeSequence(current.seq, b)
		return b.concatToString()
	}

	public fun nextSequence(): String {
		val b = CharArray(SEQ_LEN)
		writeSequence(advance().seq, b)
		return b.concatToString()
	}

	private fun advance(): State {
		while (true) {
			val current = state.load()
			val seq = current.seq + current.inc
			val next = if (seq >= MAX_SEQ) randomState() else State(current.pre, seq, current.inc)
			if (state.compareAndSet(current, next)) return next
		}
	}

	private fun writeSequence(
		seq: Long,
		into: CharArray,
	) {
		var l = seq
		for (i in into.lastIndex downTo into.size - SEQ_LEN) {
			into[i] = DIGITS[(l % BASE).toInt()]
			l /= BASE
		}
	}

	private fun randomState(): State {
		val bytes = CryptographyRandom.nextBytes(PRE_LEN)
		val pre = CharArray(PRE_LEN) { DIGITS[(bytes[it].toInt() and 0xFF) % BASE] }
		return State(
			pre = pre,
			seq = CryptographyRandom.nextLong(MAX_SEQ),
			inc = CryptographyRandom.nextLong(MIN_INC, MAX_INC),
		)
	}

	override fun toString(): String {
		val current = state.load()
		return "NUID(seq=${current.seq}, inc=${current.inc})"
	}
}
