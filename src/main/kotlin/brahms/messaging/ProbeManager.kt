package brahms.messaging

import Configs
import brahms.messaging.messages.G2GMessage
import brahms.messaging.messages.ProbeRequest
import brahms.messaging.messages.ProbeResponse
import kotlinx.coroutines.delay
import peers.Peer
import java.time.LocalDateTime

object ProbeManager : G2GMessageListener {
    val probes: HashMap<Peer, LocalDateTime> = LinkedHashMap()

    override fun receive(message: G2GMessage) {
        if (message is ProbeResponse) {
            probes.remove(message.sender)
        }
    }

    fun probe(peer: Peer) {
        G2GCommunicator.send(ProbeRequest(Configs.getConfigs().self, peer))
        probes.put(peer, LocalDateTime.now())
    }

    //    TODO: call at beginning
    suspend fun kick() {
        while (true) {
            delay(Configs.getConfigs().kickInterval)
            val oldestTime = LocalDateTime.now().minusSeconds(4L)
            for (entry in probes) {
                if (entry.value.isBefore(oldestTime)) {
                    probes.remove(entry.key)
                    entry.key.online = false
                }
            }
        }
    }
}