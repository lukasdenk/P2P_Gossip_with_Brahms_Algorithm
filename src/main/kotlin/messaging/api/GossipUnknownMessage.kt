package messaging.api

import utils.readRemaining
import java.nio.ByteBuffer

data class GossipUnknownMessage(val data: ByteArray) : APIMsg() {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): GossipUnknownMessage {
            return GossipUnknownMessage(data = buffer.readRemaining())
        }
    }

    override fun toByteArray(): ByteArray {
        return data
    }
}