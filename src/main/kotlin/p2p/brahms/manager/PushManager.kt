package p2p.brahms.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import main.Preferences
import main.startsWithXLeadingZeroes
import messaging.p2p.P2PMsg
import messaging.p2p.P2PMsgListener
import messaging.p2p.Peer
import messaging.p2p.PushMsg
import p2p.P2PCommunicator
import p2p.brahms.History
import p2p.brahms.PoW
import java.util.*
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object PushManager : P2PMsgListener {
    val pushs: MutableSet<Peer> = Collections.synchronizedSet(mutableSetOf())

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
        pushs.clear()
    }

    override fun receive(msg: P2PMsg) {
//        Ignore if sender is equals to ourselves. In this case, the push must be from an erroneous or attacker peer.
        if (msg is PushMsg && validate(msg) && msg.sender != Preferences.self) {
            History.next(msg.sender)
            pushs.add(msg.sender)
        }
    }

    private fun validate(msg: PushMsg): Boolean {
        for (i in 0 until 4) {
            if (PoW.buildPoW(System.currentTimeMillis() - 60000L * i, msg.sender, Preferences.self, msg.nonce)
                    .startsWithXLeadingZeroes(Preferences.difficulty)
            ) {
                return true
            }
        }
        return false
    }


}