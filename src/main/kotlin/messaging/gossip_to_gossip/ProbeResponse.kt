package messaging.gossip_to_gossip

import peers.Peer

class ProbeResponse(override val sender: Peer, override val receiver: Peer) : G2GMessage(sender, receiver) {
}