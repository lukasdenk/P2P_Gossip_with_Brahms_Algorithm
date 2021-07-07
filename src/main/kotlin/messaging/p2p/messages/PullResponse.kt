package messaging.p2p.messages

import peers.Peer

data class PullResponse(val neighbourSample: Set<Peer>) :
    P2PMessage() {
}