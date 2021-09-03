package utils

import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.APIMessage
import messaging.api.MessageType
import messaging.api.gossip.*
import java.nio.ByteBuffer

@ExperimentalSerializationApi
class MessageParser {


    fun toApiMessage(buffer: ByteBuffer): APIMessage {
        if (buffer.remaining() < 8) {
            return GossipUnknownMessage.fromByteBuffer(buffer)
        }
        val size = buffer.short
        val type = buffer.short
        return when(type) {
            MessageType.GossipAnnounce.value -> {
                GossipAnnounce.fromByteBuffer(buffer)
            }
            MessageType.GossipNotify.value -> {
                GossipNotify.fromByteBuffer(buffer)
            }
            MessageType.GossipNotification.value -> {
                GossipNotification.fromByteBuffer(buffer)
            }
            MessageType.GossipValidation.value -> {
                GossipValidation.fromByteBuffer(buffer)
            }
            else -> {
                GossipUnknownMessage.fromByteBuffer(buffer)
            }
        }
    }

}