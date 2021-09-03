package messaging.api.nse

import messaging.api.APIMessage

class NseEstimate(val estimation: Int) : APIMessage {
    override fun toByteArray(): ByteArray {
        TODO("Not yet implemented")
    }
}