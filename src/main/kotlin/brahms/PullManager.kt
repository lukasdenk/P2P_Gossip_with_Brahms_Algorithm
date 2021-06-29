package brahms

import Configs
import messaging.G2GCommunicator
import messaging.G2GMessageListener
import messaging.gossip_to_gossip.G2GMessage
import messaging.gossip_to_gossip.PullRequest
import messaging.gossip_to_gossip.PullResponse
import peers.Peer

object PullManager : G2GMessageListener {
    private val requests: MutableMap<Peer, PullRequest> = HashMap()
    var limit: Int = 0
    private val communicator: G2GCommunicator = G2GCommunicator.singleton

    fun pull(peers: Collection<Peer>) {
        requests.clear()
        peers.parallelStream().forEach {
            val pullRequest = PullRequest(Configs.getConfigs().self, it, limit)
            requests[it] = pullRequest
            communicator.send(pullRequest)
        }
    }

    override fun receive(message: G2GMessage) {
        if (message is PullResponse && message.sender in requests) {
            History.next(message.neighbourSample)
        }
    }


}