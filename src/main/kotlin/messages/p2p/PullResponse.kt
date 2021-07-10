package messages.p2p

import peers.Peer

data class PullResponse(val neighbourSample: Set<Peer>) :
    P2PMessage() {
}