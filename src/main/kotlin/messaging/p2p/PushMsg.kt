package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
class PushMsg(val nonce: ByteArray) : P2PMessage {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}