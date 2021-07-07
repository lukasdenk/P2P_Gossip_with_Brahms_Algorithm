package brahms.messaging.messages

import peers.Peer

open class G2GMessage(open val sender: Peer, open val receiver: Peer) {
}