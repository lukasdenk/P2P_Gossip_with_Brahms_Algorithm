package messaging.p2p

import kotlinx.serialization.Serializable
import messaging.api.Port

@Serializable
data class Peer(
    val ip: String,
    val port: Port
) {

    var online: Boolean = true
}