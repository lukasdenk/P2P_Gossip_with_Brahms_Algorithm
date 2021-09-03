package json

import kotlinx.serialization.Serializable

@Serializable
class PushMsg(val nonce: Long) : P2PMessage() {
}