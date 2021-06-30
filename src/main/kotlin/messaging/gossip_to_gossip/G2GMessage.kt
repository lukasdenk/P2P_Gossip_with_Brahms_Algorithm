package messaging.gossip_to_gossip

import peers.Peer

open class G2GMessage(open val sender: Peer, open val receiver: Peer) {
}