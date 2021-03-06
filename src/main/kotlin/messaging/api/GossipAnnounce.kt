package messaging.api

import java.nio.ByteBuffer

class GossipAnnounce(
    val ttl: Byte,
    val dataType: Short,
    val data: ByteArray
) : APIMsg() {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): GossipAnnounce {
            val ttl = buffer.get()
            buffer.get() // skip reserved byte
            val dataType = buffer.short
            return GossipAnnounce(ttl, dataType, buffer.array())
        }
    }

    override fun toByteArray(): ByteArray {
        val headerSize: Short = 8
        val buffer = ByteBuffer.allocate(headerSize + data.size)
        buffer.putShort(headerSize)
        buffer.putShort(MsgType.GossipAnnounce.value)
        buffer.put(ttl)
        buffer.put(0) // reserved byte
        buffer.putShort(dataType)
        buffer.put(data)
        buffer.position(0)
        val result = ByteArray(buffer.capacity())
        buffer.get(result, 0, result.size)
        return result
    }
}