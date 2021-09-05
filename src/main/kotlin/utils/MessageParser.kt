package utils

import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.*
import java.nio.ByteBuffer

@ExperimentalSerializationApi
class MessageParser {

    fun toApiMessage(buffer: ByteBuffer): APIMsg {
        if (buffer.remaining() < 8) {
            return GossipUnknownMessage.fromByteBuffer(buffer)
        }
        buffer.short // skip size
        val type = buffer.short
        return when(type) {
            MsgType.GossipAnnounce.value -> {
                GossipAnnounce.fromByteBuffer(buffer)
            }
            MsgType.GossipNotify.value -> {
                GossipNotify.fromByteBuffer(buffer)
            }
            MsgType.GossipNotification.value -> {
                GossipNotification.fromByteBuffer(buffer)
            }
            MsgType.GossipValidation.value -> {
                GossipValidation.fromByteBuffer(buffer)
            }
            else -> {
                GossipUnknownMessage.fromByteBuffer(buffer)
            }
        }
    }

}