package brahms

import Configs
import peers.Peer
import randomSubSet

object View {
    private val cacheSize = Configs.getConfigs().cacheSize
    var v: MutableSet<Peer> = HashSet()
    private var vPush: Set<Peer> = HashSet()
    private var vPull: Set<Peer> = HashSet()

    //    TODO: sophisticated value
    const val pushLimit: Int = 1000

    private fun update() {
        while (true) {
            PushManager.push(v.randomSubSet(Partitioner.pushSize))
            PullManager.pull(v.randomSubSet(Partitioner.pullSize))
//          TODO:  wait rand secs (in coroutine)
            val waitTime = 4
            if (vPush.size < waitTime * pushLimit) {
                v = (vPush.randomSubSet(Partitioner.pushSize) union
                        vPull.randomSubSet(Partitioner.pullSize) union
                        History.get(Partitioner.historySize)).toMutableSet()
            }
        }
    }


}