package p2p.brahms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import main.Configs
import main.randomSubSet
import messaging.p2p.Peer
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager


object View {
    //    TODO: wozu cacheSize?
    private val cacheSize = Configs.cacheSize
    var view: MutableSet<Peer> = HashSet()
    private var vPush: Set<Peer> = HashSet()
    private var vPull: Set<Peer> = HashSet()

    //    TODO: sophisticated value
    const val pushLimit: Int = 1000

    //    TODO: call at beginning
    suspend fun CoroutineScope.update() {
        while (true) {
            PushManager.push(view.randomSubSet(Partitioner.pushSize))
            PullManager.pull(view.randomSubSet(Partitioner.pullSize))
//          TODO:  wait rand secs
            val waitTime = 4L
            delay(waitTime)
            if (vPush.size < waitTime * pushLimit) {
                view = (vPush.randomSubSet(Partitioner.pushSize) union
                        vPull.randomSubSet(Partitioner.pullSize) union
                        History.get(Partitioner.historySize)).toMutableSet()
            }
        }
    }


}