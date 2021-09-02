package messaging.p2p

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messaging.api.DataType
import networking.P2PMessageType
import utils.toByteArray
import java.nio.ByteBuffer
import kotlin.math.max

@Serializable
class SpreadMsg(val dataType: DataType, var ttl: Int, val data: ByteArray) : P2PMessage {
    fun decrementTtl() {
        ttl = max(0, ttl - 1)
    }

    override fun toByteArray(): ByteArray {
        val headerSize = Int.SIZE_BYTES
        val dataTypeSize = Int.SIZE_BYTES
        val ttlSize = Int.SIZE_BYTES
        val buffer = ByteBuffer.allocate(headerSize + dataTypeSize + ttlSize + data.size)
        buffer.putInt(P2PMessageType.SpreadMessage.value)
        buffer.putShort(dataType)
        buffer.putInt(ttl)
        buffer.put(data)
        return buffer.toByteArray()
    }
}