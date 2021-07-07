package brahms.messaging.messages

import peers.Peer

class ProbeRequest(override val sender: Peer, override val receiver: Peer) : G2GMessage(sender, receiver) {
}