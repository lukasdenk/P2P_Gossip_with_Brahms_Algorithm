package p2p

import messages.p2p.P2PMessage
import peers.Peer

interface P2PMessageListener {
    fun receive(msg: P2PMessage, sender : Peer)
}