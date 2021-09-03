package messaging.p2p

import kotlinx.serialization.Serializable
import messaging.Peer

@Serializable
class PullResponse(val neighbourSample: Set<Peer>) : P2PMessage() {
}