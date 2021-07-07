package brahms.messaging

import brahms.History
import brahms.View
import brahms.messaging.messages.P2PMessage
import brahms.messaging.messages.PullRequest
import brahms.messaging.messages.PullResponse
import peers.Peer
import randomSubSet
import java.util.concurrent.ConcurrentHashMap

object PullManager : P2PMessageListener {
    private val requests: ConcurrentHashMap<Peer, PullRequest> = ConcurrentHashMap()
    var limit: Int = 0

    fun pull(peers: Collection<Peer>) {
        requests.clear()
        peers.parallelStream().forEach {
            val pullRequest = PullRequest(limit)
            requests[it] = pullRequest
            P2PCommunicator.send(pullRequest, it)
        }
    }

    override fun receive(msg: P2PMessage, sender : Peer) {
        if (msg is PullResponse && requests.containsKey(sender)) {
            History.next(msg.neighbourSample)
        } else if (msg is PullRequest) {
            P2PCommunicator.send(
                PullResponse(
                    View.view.randomSubSet(msg.limit)
                ),
                sender
            )
        }
    }


}