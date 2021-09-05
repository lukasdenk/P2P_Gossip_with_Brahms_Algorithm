package p2p.brahms.manager

import kotlinx.serialization.ExperimentalSerializationApi
import main.Preferences
import main.randomSubSet
import messaging.p2p.*
import p2p.P2PCommunicator
import p2p.brahms.History
import p2p.brahms.View
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object PullManager : P2PMsgListener {
    private val requests: ConcurrentHashMap<Peer, PullRequest> = ConcurrentHashMap()
    val receivedPulls: MutableSet<Peer> = Collections.synchronizedSet(mutableSetOf())

    fun reset() {
        receivedPulls.clear()
    }

    fun pull(peers: Collection<Peer>) {
        requests.clear()
        peers.forEach {
            val pullRequest = PullRequest(Preferences.pullLimit)
            requests[it] = pullRequest
            P2PCommunicator.send(pullRequest, it)
        }
    }

    override fun receive(msg: P2PMsg) {
        if (msg is PullResponse && requests.containsKey(msg.sender)) {
            History.next(msg.neighbourSample)
            receivedPulls.addAll(msg.neighbourSample.filter { it != Preferences.self })
        } else if (msg is PullRequest) {
            P2PCommunicator.send(
                PullResponse(
                    View.view.randomSubSet(msg.limit)
                ),
                msg.sender
            )
        }
    }


}