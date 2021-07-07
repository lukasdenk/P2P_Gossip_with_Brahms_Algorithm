package brahms

import Configs
import brahms.messaging.PullManager
import brahms.messaging.PushManager
import kotlinx.coroutines.delay
import peers.Peer
import randomSubSet

object View {
    private val cacheSize = Configs.getConfigs().cacheSize
    var v: MutableSet<Peer> = HashSet()
    private var vPush: Set<Peer> = HashSet()
    private var vPull: Set<Peer> = HashSet()

    //    TODO: sophisticated value
    const val pushLimit: Int = 1000

    //    TODO: call at beginning
    suspend fun update() {
        while (true) {
            PushManager.push(v.randomSubSet(Partitioner.pushSize))
            PullManager.pull(v.randomSubSet(Partitioner.pullSize))
//          TODO:  wait rand secs
            val waitTime = 4L
            delay(waitTime)
            if (vPush.size < waitTime * pushLimit) {
                v = (vPush.randomSubSet(Partitioner.pushSize) union
                        vPull.randomSubSet(Partitioner.pullSize) union
                        History.get(Partitioner.historySize)).toMutableSet()
            }
        }
    }


}