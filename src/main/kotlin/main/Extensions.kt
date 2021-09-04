package main

import java.security.MessageDigest
import kotlin.math.min


fun ByteArray.sha256(): ByteArray {
    val sha256Instance = MessageDigest.getInstance("SHA256")
    return sha256Instance.digest(this)
}

operator fun ByteArray.compareTo(other: ByteArray?): Int {
    if (other == null) {
        return 1
    }

    val leadingZeroesComparison = other.leadingZeroes().compareTo(this.leadingZeroes())
    if (leadingZeroesComparison != 0) {
        return leadingZeroesComparison
    }
    for (i in 0..this.size) {
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


