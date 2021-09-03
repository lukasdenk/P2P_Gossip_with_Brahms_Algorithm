package messaging.api.gossip

import messaging.api.APIMessage
import utils.readRemaining
import java.nio.ByteBuffer

data class GossipUnknownMessage(val data: ByteArray) : APIMessage {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): GossipUnknownMessage {
            return GossipUnknownMessage(data = buffer.readRemaining())
        }
    }

    override fun toByteArray(): ByteArray {
        return data
    }
}