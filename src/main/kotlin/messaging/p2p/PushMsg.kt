package messaging.p2p

import kotlinx.serialization.Serializable
import networking.P2PMessageType
import utils.toByteArray
import java.nio.ByteBuffer

@Serializable
class PushMsg(val nonce: ByteArray) : P2PMessage {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }

    override fun toByteArray(): ByteArray {
        val headerSize = Int.SIZE_BYTES
        val buffer = ByteBuffer.allocate(headerSize + nonce.size)
        buffer.putInt(P2PMessageType.PushRequest.value)
        buffer.put(nonce)
        return buffer.toByteArray()
    }
}