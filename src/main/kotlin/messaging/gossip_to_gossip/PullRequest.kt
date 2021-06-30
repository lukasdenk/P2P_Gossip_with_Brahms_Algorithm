package messaging.gossip_to_gossip

import peers.Peer

data class PullRequest(override val sender: Peer, override val receiver: Peer, val limit: Int) :
    G2GMessage(sender, receiver) {

}