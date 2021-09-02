package utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import messaging.api.*
import messaging.p2p.*
import messaging.p2p.P2PUnknownMessage
import networking.P2PMessageType
import java.nio.ByteBuffer

@ExperimentalSerializationApi
class MessageParser {

    fun toPeerToPeerMessage(buffer: ByteBuffer): P2PMessage {
        val type = buffer.int
        val body = buffer.readRemaining()
        return when(type) {
            P2PMessageType.SpreadMessage.value -> {
                Json.decodeFromString<SpreadMsg>(String(body))
            }
            P2PMessageType.PullRequest.value -> {
                Json.decodeFromString<PullRequest>(String(body))
            }
            P2PMessageType.PullResponse.value -> {
                Json.decodeFromString<PullResponse>(String(body))
            }
            P2PMessageType.PushRequest.value -> {
                Json.decodeFromString<PushMsg>(String(body))
            }
            P2PMessageType.ProbeRequest.value -> {
                ProbeRequest()
            }
            P2PMessageType.ProbeResponse.value -> {
                ProbeResponse()
            }
            else -> {
                P2PUnknownMessage(data = body)
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