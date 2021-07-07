package brahms.messaging

import Configs
import brahms.messaging.messages.P2PMessage
import brahms.messaging.messages.ProbeRequest
import brahms.messaging.messages.ProbeResponse
import kotlinx.coroutines.delay
import peers.Peer
import java.time.LocalDateTime

object ProbeManager : P2PMessageListener {
    val probes: HashMap<Peer, LocalDateTime> = LinkedHashMap()

    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is ProbeResponse) {
            probes.remove(sender)
        }
    }

    fun probe(peer: Peer) {
        P2PCommunicator.send(ProbeRequest(), peer)
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