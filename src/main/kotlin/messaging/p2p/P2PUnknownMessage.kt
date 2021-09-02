package messaging.p2p

class P2PUnknownMessage(val data: ByteArray): P2PMessage {
    override fun toByteArray(): ByteArray {
        return data
    }
}