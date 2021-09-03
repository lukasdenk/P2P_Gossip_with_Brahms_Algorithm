package p2p.brahms

import kotlinx.coroutines.delay
import main.randomSubSet
import messaging.p2p.Peer
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import kotlin.time.ExperimentalTime


object View {
    var view: MutableSet<Peer> = HashSet()

    //    TODO: sophisticated value
    const val pushLimit: Int = 1000

    @ExperimentalTime
    suspend fun update() {
        while (true) {
            PushManager.reset()
            PullManager.reset()
            PushManager.push(view.randomSubSet(Partitioner.pushSize))
            PullManager.pull(view.randomSubSet(Partitioner.pullSize))
//          TODO:  wait rand secs
            val waitTime = 4L
            if (PushManager.receivedPushs.size < waitTime * pushLimit) {
                view = (PushManager.receivedPushs.randomSubSet(Partitioner.pushSize) union
                        PullManager.receivedPulls.randomSubSet(Partitioner.pullSize) union
                        History.get(Partitioner.historySize)).toMutableSet()
            }
            delay(waitTime)
        }
    }


}