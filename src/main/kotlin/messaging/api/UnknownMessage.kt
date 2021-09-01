package messaging.api

import utils.readRemaining
import java.nio.ByteBuffer

data class UnknownMessage(val data: ByteArray) : APIMessage {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): UnknownMessage {
            return UnknownMessage(data = buffer.readRemaining())
        }
    }

    override fun toByteArray(): ByteArray {
        return data
    }
}