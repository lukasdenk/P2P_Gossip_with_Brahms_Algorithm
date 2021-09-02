package messaging.p2p

import kotlinx.serialization.Serializable
import networking.P2PMessageType
import utils.toByteArray
import java.nio.ByteBuffer

@Serializable
data class PushMsg(val nonce: Long) : P2PMessage() {

    override fun toByteArray(): ByteArray {
        val headerSize = Int.SIZE_BYTES
        val buffer = ByteBuffer.allocate(headerSize + nonce.size)
        buffer.putInt(P2PMessageType.PushRequest.value)
        buffer.put(nonce)
        return buffer.toByteArray()
    }
}