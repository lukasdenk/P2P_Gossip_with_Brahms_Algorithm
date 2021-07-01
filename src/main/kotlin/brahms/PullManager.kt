package brahms

import Configs
import messaging.G2GCommunicator
import messaging.G2GMessageListener
import messaging.gossip_to_gossip.G2GMessage
import messaging.gossip_to_gossip.PullRequest
import messaging.gossip_to_gossip.PullResponse
import peers.Peer
import randomSubSet
import java.util.concurrent.ConcurrentHashMap

object PullManager : G2GMessageListener {
    private val requests: ConcurrentHashMap<Peer, PullRequest> = ConcurrentHashMap()
    var limit: Int = 0

    fun pull(peers: Collection<Peer>) {
        requests.clear()
        peers.parallelStream().forEach {
            val pullRequest = PullRequest(Configs.getConfigs().self, it, limit)
            requests[it] = pullRequest
            G2GCommunicator.send(pullRequest)
        }
    }

    override fun receive(message: G2GMessage) {
        if (message is PullResponse && requests.containsKey(message.sender)) {
            History.next(message.neighbourSample)
        } else if (message is PullRequest) {
            G2GCommunicator.send(
                PullResponse(
                    Configs.getConfigs().self,
                    message.sender,
                    View.v.randomSubSet(message.limit)
                )
            )
        }
    }


}