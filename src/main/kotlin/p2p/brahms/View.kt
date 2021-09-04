package p2p.brahms

import kotlinx.coroutines.delay
import main.Configs
import main.randomSubSet
import messaging.p2p.Peer
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import java.util.*
import kotlin.time.ExperimentalTime


object View {
    var view: MutableSet<Peer> = Collections.synchronizedSet(Configs.seed)

    private const val alpha = 0.45
    private const val beta = 0.45

    private val pushFraction = (Configs.degree * alpha).toInt()
    private val pullFraction = (Configs.degree * beta).toInt()
    private val historyFraction = Configs.degree - pushFraction - pullFraction


    @ExperimentalTime
    suspend fun update() {
        var i = 0
        while (true) {
            PushManager.reset()
            PullManager.reset()
            PushManager.push(view.randomSubSet(pushFraction))
            PullManager.pull(view.randomSubSet(pullFraction))

            delay(Configs.updateInterval)

            if (PushManager.receivedPushs.size < Configs.pushLimit) {
                val pushs = PushManager.receivedPushs.randomSubSet(pushFraction)
                val pulls = PullManager.receivedPulls.randomSubSet(pullFraction)
                val pushsAndPulls = pushs union pulls
                val compensate = pullFraction + pushFraction - pushsAndPulls.size
                view = (pushsAndPulls union
                        History.get(historyFraction + compensate)).toMutableSet()
            }
            i++
        }
    }


}