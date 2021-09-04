package messaging.p2p

interface P2PMsgListener {
    fun receive(msg: P2PMsg)
}