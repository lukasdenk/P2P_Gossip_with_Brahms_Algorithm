package brahms.messaging

import brahms.messaging.messages.P2PMessage
import peers.Peer

interface P2PMessageListener {
    fun receive(msg: P2PMessage, sender : Peer)
}