package peers

import java.util.*

data class Peer(
    val id: UUID = UUID.randomUUID(),
    val ip: String,
    val port: String
): Comparable<Peer> {
    override fun compareTo(other: Peer): Int =
        Comparator.comparing(Peer::id).compare(this, other)
}