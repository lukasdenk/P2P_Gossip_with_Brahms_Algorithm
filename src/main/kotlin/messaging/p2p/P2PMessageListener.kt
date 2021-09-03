package messaging.p2p

interface P2PMessageListener {
    fun receive(msg: P2PMessage, sender: Peer)
}