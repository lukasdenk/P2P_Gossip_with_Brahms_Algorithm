package p2p.brahms

import kotlinx.coroutines.delay
import main.Configs
import main.randomSubSet
import messaging.p2p.Peer
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import kotlin.time.ExperimentalTime


object View {
    var view: MutableSet<Peer> = HashSet()

    private val cacheSize = Configs.cacheSize

    val pushSize = (Configs.alpha * cacheSize).toInt()
    val pullSize = (Configs.beta * cacheSize).toInt()
    val historySize = cacheSize - pullSize - pushSize

    //    TODO: sophisticated value
    const val pushLimit: Int = 1000

    @ExperimentalTime
    suspend fun update() {
        while (true) {
            PushManager.reset()
            PullManager.reset()
            PushManager.push(view.randomSubSet(pushSize))
            PullManager.pull(view.randomSubSet(pullSize))

            delay(Configs.updateInterval)

            if (PushManager.receivedPushs.size < pushLimit) {
                val pushs = PushManager.receivedPushs.randomSubSet(pushSize)
                val pulls = PullManager.receivedPulls.randomSubSet(pullSize)
                val pushsAndPulls = pushs union pulls
                val tooLess = pullSize + pushSize - pushsAndPulls.size
                view = (pushsAndPulls union
                        History.get(historySize + tooLess)).toMutableSet()
            }
        }
    }


}