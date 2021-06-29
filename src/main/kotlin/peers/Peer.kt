package peers

data class Peer(
//    TODO: Store hostkey as Hexvalue
    val hostkey: String,
    val ip: String,
    val port: String
): Comparable<Peer> {
    override fun compareTo(other: Peer): Int =
        Comparator.comparing(Peer::hostkey).compare(this, other)

    fun probe(): Boolean {
//        TODO: test if Peer is online (not only the computer but the VoIP-program!)
        return false
    }
}