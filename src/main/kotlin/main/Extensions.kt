package main

import java.security.MessageDigest
import kotlin.math.min

/**
 * Hashes a ByteArray
 */
fun ByteArray.sha256(): ByteArray {
    val sha256Instance = MessageDigest.getInstance("SHA256")
    val digest = sha256Instance.digest(this)
    if (digest.size != 32) {
        throw IllegalStateException("digest size not 32. $this")
    }
    return digest
}


/**
 * Compares two ByteArray according to the binary number they present.
 */
operator fun ByteArray.compareTo(other: ByteArray?): Int {
    if (other == null) {
        return 1
    }
    if (this.size != other.size) {
        throw UnsupportedOperationException("Can only compare ByteArrays with identical length")
    }

    for (i in 0 until this.size) {
        val comp = this[i].compareTo(other[i])
        if (comp != 0) {
            return comp
        }
    }
    return 0
}

fun ByteArray.startsWithXLeadingZeroes(x: Int): Boolean {
    return this.leadingZeroes() >= x
}

fun ByteArray.leadingZeroes(): Int {
    var leadingZeroes = 0
    for (i in 0..this.size) {
        val firstOneBit = Integer.toBinaryString(this[i].toInt()).indexOf('1')
        if (firstOneBit == -1) {
            leadingZeroes += 8
        } else {
            leadingZeroes += firstOneBit
            return leadingZeroes
        }
    }
    return leadingZeroes
}

fun <T> Set<T>.randomSubSet(n: Int): MutableSet<T> {
    val ceiling = min(n, this.size)
    return this.toList().shuffled().subList(0, ceiling).toMutableSet()
}


