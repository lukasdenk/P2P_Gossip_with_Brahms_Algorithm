package p2p.brahms

import api.manager.NseMsgsManager
import kotlinx.coroutines.delay
import main.Configs
import main.randomSubSet
import messaging.p2p.Peer
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import java.util.*
import kotlin.math.pow
import kotlin.time.ExperimentalTime


object View {
    var view: MutableSet<Peer> = Collections.synchronizedSet(Configs.seed)

    private const val alpha = 0.45
    private const val beta = 0.45

    private var pushFraction = (Configs.initNse3rdRoot * alpha).toInt()
    private var pullFraction = (Configs.initNse3rdRoot * beta).toInt()
    private var historyFraction = Configs.initNse3rdRoot - pushFraction - pullFraction


    @ExperimentalTime
    suspend fun update() {
        var i = 0
        while (true) {
            if (i % Configs.estimationUpdateInterval == 0) {
                updateEstimation()
            }

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

    @ExperimentalTime
    private fun updateEstimation() {
        val estimation = NseMsgsManager.estimation
        val historyAndViewSize = estimation.toDouble().pow(1.0 / 3.0).toInt()
        pushFraction = (alpha * historyAndViewSize).toInt()
        pullFraction = (beta * historyAndViewSize).toInt()
        historyFraction = historyAndViewSize - pullFraction - pushFraction
        view = view.randomSubSet(historyAndViewSize)
        History.resize(historyAndViewSize)
    }


}