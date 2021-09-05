package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import main.Preferences
import messaging.api.DataType
import kotlin.math.max

@Serializable
@SerialName("SpreadMsg")
class SpreadMsg(
    val dataType: DataType,
    var ttl: Int,
    val data: ByteArray,
    override val sender: Peer = Preferences.self
) :
    P2PMsg() {
    fun decrementTtl() {
        if (ttl != 0) {
            ttl = max(1, ttl - 1)
        }
    }
}