package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import main.Preferences

@Serializable
@SerialName("PushMsg")
class PushMsg(val nonce: Long, override val sender: Peer = Preferences.self) : P2PMsg() {
}