package brahms.messaging.messages

import peers.Peer

data class PullRequest(override val sender: Peer, override val receiver: Peer, val limit: Int) :
    P2PMessage(sender, receiver) {

}