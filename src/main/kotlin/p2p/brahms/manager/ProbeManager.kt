package p2p.brahms.manager

import kotlinx.coroutines.delay
import main.Configs
import messaging.p2p.*
import p2p.P2PCommunicator
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
            delay(Configs.kickInterval)
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