package peers

data class Peer(
//    TODO: Store hostkey as Hexvalue
    val hostkey: ByteArray,
    val ip: String,
    val port: String
//    @Kyrylo: Why do you need Peers to be comparable?
) {

    fun probe(): Boolean {
//        TODO: test if Peer is online (not only the computer but the VoIP-program!)
        return false
    }

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