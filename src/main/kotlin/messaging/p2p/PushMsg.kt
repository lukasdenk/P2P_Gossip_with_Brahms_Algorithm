package messaging.p2p

import kotlinx.serialization.Serializable
import main.Preferences

@Serializable
class PushMsg(val nonce: Long, override val sender: Peer = Preferences.self) : P2PMsg() {
}