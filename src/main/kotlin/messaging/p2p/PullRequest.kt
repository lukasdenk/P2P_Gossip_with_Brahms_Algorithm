package messaging.p2p

import kotlinx.serialization.Serializable
import main.Preferences

@Serializable
class PullRequest(val limit: Int, override val sender: Peer = Preferences.self) : P2PMessage() {
}