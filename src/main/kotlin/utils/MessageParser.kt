package utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import messaging.api.*
import messaging.p2p.*
import messaging.p2p.P2PUnknownMessage
import java.nio.ByteBuffer

@ExperimentalSerializationApi
class MessageParser {

    fun toPeerToPeerMessage(buffer: ByteBuffer): P2PMessage {
        val type = buffer.int
        val body = ByteArray(buffer.capacity())
        buffer.get(body, Int.SIZE_BYTES, body.size)
        return when(type) {
            MessageType.SpreadMessage.value.toInt() -> {
                Json.decodeFromString<SpreadMsg>(String(body))
            }
            MessageType.PullRequest.value.toInt() -> {
                Json.decodeFromString<PullRequest>(String(body))
            }
            MessageType.PullResponse.value.toInt() -> {
                Json.decodeFromString<PullResponse>(String(body))
            }
            MessageType.PushRequest.value.toInt() -> {
                Json.decodeFromString<PushMsg>(String(body))
            }
            MessageType.ProbeRequest.value.toInt() -> {
                ProbeRequest()
            }
            MessageType.ProbeResponse.value.toInt() -> {
                ProbeResponse()
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