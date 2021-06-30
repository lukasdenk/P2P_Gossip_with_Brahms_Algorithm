import java.security.MessageDigest
import kotlin.math.min

fun ByteArray.sha256(): ByteArray {
    val sha256Instance = MessageDigest.getInstance("SHA256")
    return sha256Instance.digest(this)
}

operator fun ByteArray.compareTo(other: ByteArray): Int {
    val lengthComparison = this.size.compareTo(other.size)
    if (lengthComparison != 0) {
        return lengthComparison
    }
    for (i in 0..this.size) {
        val comp = this[i].compareTo(other[i])
        if (comp != 0) {
            return comp
        }
    }
    return 1
}

fun <T> Collection<T>.randomSubSet(n: Int): MutableSet<T> {
    val ceiling = min(n, this.size)
    return this.toList().shuffled().subList(0, ceiling).toMutableSet()
}
