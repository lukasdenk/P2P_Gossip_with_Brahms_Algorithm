package p2p.brahms.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import main.Configs
import main.startsWithXLeadingZeroes
import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import messaging.p2p.PushMsg
import p2p.P2PCommunicator
import p2p.brahms.History
import p2p.brahms.PoW
import java.util.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
object PushManager : P2PMessageListener {
    val receivedPushs: MutableSet<Peer> = Collections.synchronizedSet(mutableSetOf())

    fun push(peers: Set<Peer>) {
        peers.forEach {
            CoroutineScope(Dispatchers.Main).launch {
                val nonce = PoW.work(it)
                val msg = PushMsg(nonce)
                P2PCommunicator.send(msg, it)
            }
        }
    }

    fun reset() {
        receivedPushs.clear()
    }

    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is PushMsg && validate(msg, sender)) {
            History.next(setOf(sender))
            receivedPushs.add(sender)
        }
    }

    private fun validate(msg: PushMsg, peer: Peer): Boolean {
        for (i in 0..1) {
            if (PoW.buildPoW(System.currentTimeMillis() - 60000L * i, peer, msg.nonce)
                    .startsWithXLeadingZeroes(Configs.difficulty)
            ) {
                return true
            }
        }
        return false
    }


}