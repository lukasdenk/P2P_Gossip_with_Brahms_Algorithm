package p2p.brahms.manager

import main.Configs
import main.randomSubSet
import messaging.p2p.*
import p2p.P2PCommunicator
import p2p.brahms.History
import p2p.brahms.View
import java.util.concurrent.ConcurrentHashMap

object PullManager : P2PMessageListener {
    private val requests: ConcurrentHashMap<Peer, PullRequest> = ConcurrentHashMap()
    var limit: Int = Configs.pullLimit
    val receivedPulls: MutableSet<Peer> = mutableSetOf()

    fun reset() {
        receivedPulls.clear()
    }

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
            receivedPulls.addAll(msg.neighbourSample)
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