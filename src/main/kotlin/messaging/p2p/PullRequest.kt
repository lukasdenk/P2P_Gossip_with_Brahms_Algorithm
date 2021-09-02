package messaging.p2p

import kotlinx.serialization.Serializable
import networking.P2PMessageType
import utils.toByteArray
import java.nio.ByteBuffer

@Serializable
data class PullRequest(val limit: Int) : P2PMessage {
    override fun toByteArray(): ByteArray {
        val headerSize = Int.SIZE_BYTES
        val limitSize = Int.SIZE_BYTES
        val buffer = ByteBuffer.allocate(headerSize + limitSize)
        buffer.putInt(P2PMessageType.PullRequest.value)
        buffer.putInt(limit)
        return buffer.toByteArray()
    }
}