package messaging

import json.P2PMessage

interface P2PMessageListener {
    fun receive(msg: P2PMessage, sender: Peer)
}