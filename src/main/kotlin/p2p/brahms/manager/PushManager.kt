package p2p.brahms.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import main.Preferences
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
            CoroutineScope(Dispatchers.Default).launch {
                val nonce = PoW.work(it)
                val msg = PushMsg(nonce)
                P2PCommunicator.send(msg, it)
            }
        }
    }

    fun reset() {
        receivedPushs.clear()
    }

    override fun receive(msg: P2PMessage) {
//        Ignore if sender is equals to ourselves. In this case, the push must be from an erroneous or attacker peer.
        if (msg is PushMsg && validate(msg) && msg.sender != Preferences.self) {
            History.next(msg.sender)
            receivedPushs.add(msg.sender)
        }
    }

    private fun validate(msg: PushMsg): Boolean {
        for (i in 0..4) {
            val work = PoW.buildPoW(System.currentTimeMillis() - 60000L * i, msg.sender, Preferences.self, msg.nonce)
            if (work
                    .startsWithXLeadingZeroes(Preferences.difficulty)
            ) {
                return true
            }
        }
        return false
    }


}