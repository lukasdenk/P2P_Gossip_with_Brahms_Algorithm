package utils

import messaging.api.*
import messaging.p2p.*
import messaging.p2p.P2PUnknownMessage
import java.nio.ByteBuffer

class MessageParser {

    fun toPeerToPeerMessage(buffer: ByteBuffer): P2PMessage {
        val type = buffer.int
        return when(type) {
            MessageType.SpreadMessage.value.toInt() -> {
                P2PUnknownMessage()
            }
            MessageType.PullRequest.value.toInt() -> {
                P2PUnknownMessage()
            }
            MessageType.PullResponse.value.toInt() -> {
                P2PUnknownMessage()
            }
            MessageType.PushRequest.value.toInt() -> {
                P2PUnknownMessage()
            }
            MessageType.ProbeRequest.value.toInt() -> {
                P2PUnknownMessage()
            }
            MessageType.ProbeResponse.value.toInt() -> {
                P2PUnknownMessage()
            }
            else -> {
                P2PUnknownMessage()
            }
        }
    }

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