package brahms

import CommonUtils
import Configs
import peers.Peer

object View {
    private val cacheSize = Configs.getConfigs().cacheSize
    var v: MutableSet<Peer> = HashSet()
    var vPush: Set<Peer> = HashSet()
    var vPull: Set<Peer> = HashSet()

    //    TODO: sophisticated value
    const val pushLimit: Int = 1000

    private fun update() {
        while (true) {
            PushManager.push(CommonUtils.randomSubSet(v, Partitioner.pushSize))
            PullManager.pull(CommonUtils.randomSubSet(v, Partitioner.pullSize))
//          TODO:  wait rand secs
            val waitTime = 4
            if (vPush.size < waitTime * pushLimit) {
                v = (CommonUtils.randomSubSet(vPush, Partitioner.pushSize) union
                        CommonUtils.randomSubSet(vPull, Partitioner.pullSize) union
                        History.get(Partitioner.historySize)).toMutableSet()
            }
        }
    }


}