package messaging.api

import utils.readRemaining
import java.nio.ByteBuffer

class GossipNotification(
    val messageId: Short,
    val dataType: DataType,
    val data: ByteArray
) : APIMsg() {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): GossipNotification {
            val messageId = buffer.short
            val dataType = buffer.short
            return GossipNotification(
                messageId = messageId,
                dataType = dataType,
                data = buffer.readRemaining()
            )
        }
    }

    override fun toByteArray(): ByteArray {
        val headerSize: Short = 8
        val buffer = ByteBuffer.allocate(headerSize + data.size)
        buffer.putShort(headerSize)
        buffer.putShort(MsgType.GossipNotification.value)
        buffer.putShort(messageId)
        buffer.putShort(dataType)
        buffer.put(data)
        buffer.position(0)
        val result = ByteArray(buffer.capacity())
        buffer.get(result, 0, result.size)
        return result
    }
}