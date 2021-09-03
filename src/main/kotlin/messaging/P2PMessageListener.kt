package messaging

import messaging.p2p.P2PMessage

interface P2PMessageListener {
    fun receive(msg: P2PMessage, sender: Peer)
}