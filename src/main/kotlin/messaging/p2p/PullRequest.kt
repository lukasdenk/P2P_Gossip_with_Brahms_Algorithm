package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import main.Preferences

@Serializable
@SerialName("PullRequest")
class PullRequest(val limit: Int, override val sender: Peer = Preferences.self) : P2PMsg() {
}