package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
data class PullResponse(val neighbourSample: Array<Peer>) : P2PMessage