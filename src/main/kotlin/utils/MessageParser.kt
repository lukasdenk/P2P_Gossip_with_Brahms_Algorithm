package utils

import messaging.api.*
import java.nio.ByteBuffer
import kotlin.experimental.and

class MessageParser {

    companion object {
        const val GossipAnnounce: Short = 500
        const val GossipNotify: Short = 501
        const val GossipNotification: Short = 502
        const val GossipValidation: Short = 503
    }

    fun toApiMessage(buffer: ByteBuffer): APIMessage {
        if (buffer.remaining() < 8) {
            return UnknownMessage(buffer.readRemaining())
        }
        val size = buffer.short
        val type = buffer.short
        return when(type) {
            GossipAnnounce -> {
                val ttl = buffer.get()
                buffer.get() // skip reserved byte
                val dataType = buffer.short
                GossipAnnounce(ttl, dataType, buffer.array())
            }
            GossipNotify -> {
                buffer.short // skip reserved byte
                GossipNotify(dataType = buffer.short)
            }
            GossipNotification -> {
                val messageId = buffer.short
                val dataType = buffer.short
                GossipNotification(
                    messageId = messageId,
                    dataType = dataType,
                    data = buffer.readRemaining()
                )
            }
            GossipValidation -> {
                val messageId = buffer.short
                val isValid: Boolean = buffer.short.and(1) == 1.toShort()
                GossipValidation(
                    messageId = messageId,
                    isValid = isValid
                )
            }
            else -> {
                UnknownMessage(
                    data = buffer.readRemaining()
                )
            }
        }
    }

    private fun ByteBuffer.readRemaining(): ByteArray {
        val remaining = ByteArray(this.remaining())
        var byteArrayPosition = 0
        while (this.hasRemaining()) {
            remaining[byteArrayPosition++] = this.get()
        }
        return remaining
    }

}