package messaging.p2p

import kotlinx.serialization.Serializable
import main.Preferences

@Serializable
class PullResponse(val neighbourSample: Set<Peer>, override val sender: Peer = Preferences.self) : P2PMessage() {
}