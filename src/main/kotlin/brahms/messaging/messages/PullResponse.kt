package brahms.messaging.messages

import peers.Peer

data class PullResponse(val neighbourSample: Set<Peer>) :
    P2PMessage() {
}