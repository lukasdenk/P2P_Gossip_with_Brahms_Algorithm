package messaging.p2p

import networking.P2PMessageType
import utils.toByteArray
import java.nio.ByteBuffer

class ProbeResponse : P2PMessage {
    override fun toByteArray(): ByteArray {
        val headerSize = Int.SIZE_BYTES
        val buffer = ByteBuffer.allocate(headerSize)
        buffer.putInt(P2PMessageType.ProbeRequest.value)
        return buffer.toByteArray()
    }
}