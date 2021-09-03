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
    var view: MutableSet<Peer> = Collections.synchronizedSet(mutableSetOf())

    private const val alpha = 0.45
    private const val beta = 0.45

    private var pushFraction = (Configs.initNse3rdRoot * alpha).toInt()
    private var pullFraction = (Configs.initNse3rdRoot * beta).toInt()
    private var historyFraction = Configs.initNse3rdRoot - pushFraction - pullFraction


    @ExperimentalTime
    suspend fun update() {
        var i = 0
        var pushSize = 45
        var pullSize = 45
        var historySize = 10
        while (true) {
            if (i % Configs.estimationUpdateInterval == 0) {
                updateEstimation()
            }


            PushManager.reset()
            PullManager.reset()
            PushManager.push(view.randomSubSet(pushSize))
            PullManager.pull(view.randomSubSet(pullSize))

            delay(Configs.updateInterval)

            if (PushManager.receivedPushs.size < Configs.pushLimit) {
                val pushs = PushManager.receivedPushs.randomSubSet(pushSize)
                val pulls = PullManager.receivedPulls.randomSubSet(pullSize)
                val pushsAndPulls = pushs union pulls
                val tooLess = pullSize + pushSize - pushsAndPulls.size
                view = (pushsAndPulls union
                        History.get(historySize + tooLess)).toMutableSet()
            }
        }
    }

    private fun updateEstimation() {
        val estimation = NseMsgsManager.estimation
        val historyAndViewSize = estimation.toDouble().pow(1.0 / 3.0).toInt()
        pushFraction = (alpha * estimation).toInt()
        pullFraction = (beta * estimation).toInt()
        historyFraction = estimation - pullFraction - pushFraction
        view = view.randomSubSet(historyAndViewSize)
        History.resize(historyAndViewSize)
    }


}