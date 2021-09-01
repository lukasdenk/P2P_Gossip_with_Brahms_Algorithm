package utils

import messaging.api.*
import java.nio.ByteBuffer
import kotlin.experimental.and

class MessageParser {

    fun toApiMessage(buffer: ByteBuffer): APIMessage {
        if (buffer.remaining() < 8) {
            return UnknownMessage.fromByteBuffer(buffer)
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
                UnknownMessage.fromByteBuffer(buffer)
            }
        }
    }

}