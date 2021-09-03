package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
class PushMsg(val nonce: Long) : P2PMessage() {
}