package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
class Peer(
    val ip: String,
    val port: Int
) {
    //    TODO: remove
    var online: Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Peer

        if (ip != other.ip) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ip.hashCode()
        result = 31 * result + port.hashCode()
        return result
    }

    fun toSocketAddress(): String {
        return "$ip:$port"
    }


}