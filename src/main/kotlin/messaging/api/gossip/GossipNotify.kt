package messaging.api.gossip

import messaging.api.APIMessage
import messaging.api.DataType
import messaging.api.MessageType
import java.nio.ByteBuffer

class GossipNotify(
    val dataType: DataType
) : APIMessage {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): GossipNotify {
            buffer.short // skip reserved byte
            return GossipNotify(dataType = buffer.short)
        }
    }

    override fun toByteArray(): ByteArray {
        val headerSize: Short = 8
        val buffer = ByteBuffer.allocate(headerSize.toInt())
        buffer.putShort(headerSize)
        buffer.putShort(MessageType.GossipNotify.value)
        buffer.putShort(0) // reserved
        buffer.putShort(dataType)
        buffer.position(0)
        val result = ByteArray(buffer.capacity())
        buffer.get(result, 0, result.size)
        return result
    }
}