package brahms.messaging.messages

import peers.Peer

data class PullResponse(override val sender: Peer, override val receiver: Peer, val neighbourSample: Set<Peer>) :
    G2GMessage(sender, receiver) {
}