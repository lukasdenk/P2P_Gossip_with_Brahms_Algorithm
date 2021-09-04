package messaging.p2p

import kotlinx.serialization.Serializable
import messaging.api.DataType
import kotlin.math.max

@Serializable
class SpreadMsg(val dataType: DataType, var ttl: Int, val data: ByteArray) :
    P2PMessage() {
    fun decrementTtl() {
        if (ttl != 0) {
            ttl = max(1, ttl - 1)
        }
    }
}