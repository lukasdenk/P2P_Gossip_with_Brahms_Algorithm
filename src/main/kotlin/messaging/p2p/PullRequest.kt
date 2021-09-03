package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
class PullRequest(val limit: Int) : P2PMessage() {
}