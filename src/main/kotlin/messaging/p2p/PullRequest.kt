package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
data class PullRequest(val limit: Int) : P2PMessage