package messaging.gossip_to_gossip

import peers.Peer

data class PushRequest(override val sender: Peer, override val receiver: Peer, val work: Int) :
    G2GMessage(sender, receiver) {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}