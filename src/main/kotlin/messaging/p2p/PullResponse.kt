package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import messaging.Peer

@Serializable
@SerialName("e")
class PullResponse(val neighbourSample: Set<Peer>) : P2PMessage() {
}