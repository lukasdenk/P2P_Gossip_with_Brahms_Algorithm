package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("d")
class PullRequest(val limit: Int) : P2PMessage() {

}