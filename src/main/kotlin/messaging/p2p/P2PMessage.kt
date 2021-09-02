package messaging.p2p

interface P2PMessage {
    fun toByteArray(): ByteArray
}