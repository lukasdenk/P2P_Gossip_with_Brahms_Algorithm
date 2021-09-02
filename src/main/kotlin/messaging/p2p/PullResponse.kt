package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
data class PullResponse(val neighbourSample: Set<Peer>) : P2PMessage