package messaging.p2p

data class Peer(
    val ip: String,
    val port: String
) {
    var online: Boolean = true
}