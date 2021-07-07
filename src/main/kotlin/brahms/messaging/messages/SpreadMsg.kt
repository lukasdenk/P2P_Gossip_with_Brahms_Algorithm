package brahms.messaging.messages

import messaging.gossip.DataType
import kotlin.math.max

class SpreadMsg(val dataType: DataType, var ttl: Int, val data: ByteArray) :
    P2PMessage() {
    fun decrementTtl() {
        ttl = max(0, ttl - 1)
    }
}