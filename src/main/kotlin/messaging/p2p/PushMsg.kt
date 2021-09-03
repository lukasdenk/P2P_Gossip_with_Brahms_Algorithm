package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f")
class PushMsg(val nonce: Long) : P2PMessage() {
}