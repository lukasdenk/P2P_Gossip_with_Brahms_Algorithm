package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
class PullResponse(val neighbourSample: Set<Peer>) : P2PMessage() {
}