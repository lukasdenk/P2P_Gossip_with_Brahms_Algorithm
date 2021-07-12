package p2p.brahms.manager

import main.randomSubSet
import messaging.p2p.*
import p2p.P2PCommunicator
import p2p.brahms.History
import p2p.brahms.View
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
//            TODO: update view.pullV
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