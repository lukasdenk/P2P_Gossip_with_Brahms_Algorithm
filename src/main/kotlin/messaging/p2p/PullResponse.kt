package messaging.p2p

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import networking.P2PMessageType
import utils.toByteArray
import java.nio.ByteBuffer

// TODO set can be a problem in json parsing
@ExperimentalSerializationApi
@Serializable
data class PullResponse(val neighbourSample: Set<Peer>) : P2PMessage {
    override fun toByteArray(): ByteArray {
        val headerSize = Int.SIZE_BYTES
        val body = Json.encodeToString(neighbourSample).toByteArray()
        val buffer = ByteBuffer.allocate(headerSize + body.size)
        buffer.putInt(P2PMessageType.PullResponse.value)
        buffer.put(body)
        return buffer.toByteArray()
    }
}