package json

import kotlinx.serialization.Serializable
import messaging.api.DataType
import kotlin.math.max

@Serializable
class SpreadMsg(val dataType: DataType, var ttl: Int, val data: ByteArray) :
    Super() {
    fun decrementTtl() {
        ttl = max(0, ttl - 1)
    }
}