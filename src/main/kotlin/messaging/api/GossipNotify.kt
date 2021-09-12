package messaging.api

import java.nio.ByteBuffer

class GossipNotify(
    val dataType: DataType
) : APIMsg() {

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
        buffer.putShort(MsgType.GossipNotify.value)
        buffer.putShort(0) // reserved
        buffer.putShort(dataType)
        buffer.position(0)
        val result = ByteArray(buffer.capacity())
        buffer.get(result, 0, result.size)
        return result
    }
}