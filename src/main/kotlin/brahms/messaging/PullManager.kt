package brahms.messaging

import Configs
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
            val pullRequest = PullRequest(Configs.getConfigs().self, it, limit)
            requests[it] = pullRequest
            P2PCommunicator.send(pullRequest)
        }
    }

    override fun receive(msg: P2PMessage) {
        if (msg is PullResponse && requests.containsKey(msg.sender)) {
            History.next(msg.neighbourSample)
        } else if (msg is PullRequest) {
            P2PCommunicator.send(
                PullResponse(
                    Configs.getConfigs().self,
                    msg.sender,
                    View.view.randomSubSet(msg.limit)
                )
            )
        }
    }


}