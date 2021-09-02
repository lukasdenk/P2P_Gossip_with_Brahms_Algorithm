package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
data class Peer(
    val ip: String,
    val port: String
) {
    var online: Boolean = true
}