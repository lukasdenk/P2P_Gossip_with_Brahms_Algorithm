package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import main.Preferences

@Serializable
@SerialName("PullResponse")
class PullResponse(val neighbourSample: Set<Peer>, override val sender: Peer = Preferences.self) : P2PMsg() {
}