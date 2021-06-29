package messaging.gossip_to_gossip

import peers.Peer

data class PullResponse(override val sender: Peer, override val receiver: Peer, val neighbourSample: Set<Peer>) :
    G2GMessage(sender, receiver) {
}