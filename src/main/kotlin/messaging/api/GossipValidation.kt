package messaging.api

import java.nio.ByteBuffer
import kotlin.experimental.and

class GossipValidation(
    val messageId: Short,
    val isValid: Boolean
) : APIMsg {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): APIMsg {
            val messageId = buffer.short
            val isValid: Boolean = buffer.short.and(1) == 1.toShort()
            return GossipValidation(
                messageId = messageId,
                isValid = isValid
            )
        }
    }

    override fun toByteArray(): ByteArray {
        val headerSize: Short = 8
        val buffer = ByteBuffer.allocate(headerSize.toInt())
        buffer.putShort(headerSize)
        buffer.putShort(MsgType.GossipValidation.value)
        buffer.putShort(messageId)
        buffer.putShort(if (isValid) 0 else 1)
        buffer.position(0)
        val result = ByteArray(buffer.capacity())
        buffer.get(result, 0, result.size)
        return result
    }
}