package messaging.p2p

data class Peer(
//    TODO: Store hostkey as Hexvalue
    val hostkey: ByteArray,
    val ip: String,
    val port: String
//    @Kyrylo: Why do you need Peers to be comparable?
) {

    var online: Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Peer

        if (!hostkey.contentEquals(other.hostkey)) return false

        return true
    }

    override fun hashCode(): Int {
        return hostkey.contentHashCode()
    }
}