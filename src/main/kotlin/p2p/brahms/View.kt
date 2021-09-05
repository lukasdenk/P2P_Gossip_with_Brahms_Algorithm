package p2p.brahms

import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import main.Preferences
import main.randomSubSet
import messaging.p2p.Peer
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import java.util.*
import kotlin.time.ExperimentalTime


@ExperimentalSerializationApi
object View {
    var view: MutableSet<Peer> = Collections.synchronizedSet(HashSet(Preferences.bootstrappingPeers))

    private const val alpha = 0.45
    private const val beta = 0.45

    private val pushFraction = (Preferences.degree * alpha).toInt()
    private val pullFraction = (Preferences.degree * beta).toInt()
    private val historyFraction = Preferences.degree - pushFraction - pullFraction


    @ExperimentalTime
    suspend fun update() {
        var i = 0
        while (true) {
            PushManager.reset()
            PullManager.reset()
            PushManager.push(view.randomSubSet(pushFraction))
            PullManager.pull(view.randomSubSet(pullFraction))

            delay(Preferences.updateInterval)

            val oldView = setOf(view)

            if (PushManager.receivedPushs.size < Preferences.pushLimit) {
                val pushs = PushManager.receivedPushs.randomSubSet(pushFraction)
                val pulls = PullManager.receivedPulls.randomSubSet(pullFraction)
                val pushsAndPulls = pushs union pulls
                view.clear()
                view.addAll(
                    (pushsAndPulls union
                            History.get(Preferences.degree - pushsAndPulls.size)).toMutableSet()
                )
            }
            if ((view intersect oldView).size != (view union oldView).size || i % 10 == 0) {
                println("View: $view")
            }
            i++
        }
    }


}