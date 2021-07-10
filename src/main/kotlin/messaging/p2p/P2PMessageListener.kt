package messaging.p2p

import messaging.p2p.messages.P2PMessage
import peers.Peer

interface P2PMessageListener {
    fun receive(msg: P2PMessage, sender : Peer)
}