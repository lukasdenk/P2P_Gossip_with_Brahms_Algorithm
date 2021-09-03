package messaging.api.nse

import messaging.api.APIMessage
import java.lang.UnsupportedOperationException
import java.nio.ByteBuffer

class NseEstimate(
    val estimatePeers: Int
) : APIMessage {

    companion object {
        fun fromByteBuffer(buffer: ByteBuffer): APIMessage {
            val estimatePeers = buffer.int
            val estimateStdDeviation = buffer.int // ignored
            return NseEstimate(
                estimatePeers = estimatePeers
            )
        }
    }

    override fun toByteArray(): ByteArray {
        throw UnsupportedOperationException("This operation is not supported in Gossip module.")
    }
}