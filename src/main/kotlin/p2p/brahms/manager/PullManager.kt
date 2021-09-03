package p2p.brahms.manager

import main.Configs
import main.randomSubSet
import messaging.p2p.*
import p2p.P2PCommunicator
import p2p.brahms.History
import p2p.brahms.View
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

object PullManager : P2PMessageListener {
    private val requests: ConcurrentHashMap<Peer, PullRequest> = ConcurrentHashMap()
    val receivedPulls: MutableSet<Peer> = Collections.synchronizedSet(mutableSetOf())

    fun reset() {
        receivedPulls.clear()
    }

    @ExperimentalTime
    fun pull(peers: Collection<Peer>) {
        requests.clear()
        peers.forEach {
            val pullRequest = PullRequest(Configs.pullLimit)
            requests[it] = pullRequest
            P2PCommunicator.send(pullRequest, it)
        }
    }

    @ExperimentalTime
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