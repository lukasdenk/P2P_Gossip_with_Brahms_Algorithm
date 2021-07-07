package messaging.p2p.manager

import Configs
import kotlinx.coroutines.delay
import messaging.p2p.messages.P2PMessage
import messaging.p2p.messages.ProbeRequest
import messaging.p2p.messages.ProbeResponse
import messaging.p2p.P2PCommunicator
import messaging.p2p.P2PMessageListener
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