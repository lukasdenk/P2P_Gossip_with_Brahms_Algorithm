package brahms.messaging.messages

import peers.Peer

open class P2PMessage(open val sender: Peer, open val receiver: Peer) {
}