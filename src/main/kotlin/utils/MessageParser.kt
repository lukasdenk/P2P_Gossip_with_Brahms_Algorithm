package utils

import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.APIMessage
import messaging.api.MsgType
import messaging.api.gossip.*
import messaging.api.nse.NseEstimate
import messaging.api.nse.NseQuery
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
            MsgType.NseQuery.value -> {
                NseQuery()
            }
            MsgType.NseEstimate.value -> {
                NseEstimate.fromByteBuffer(buffer)
            }
            else -> {
                GossipUnknownMessage.fromByteBuffer(buffer)
            }
        }
    }

}