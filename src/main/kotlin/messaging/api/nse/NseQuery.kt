package messaging.api.nse

import messaging.api.APIMessage
import java.lang.UnsupportedOperationException

class NseQuery : APIMessage {
    override fun toByteArray(): ByteArray {
        throw UnsupportedOperationException("This operation is not supported in Gossip module.")
    }
}