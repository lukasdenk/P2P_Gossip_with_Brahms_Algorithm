package p2p

import messaging.p2p.SpreadMsg
import p2p.brahms.View
import kotlin.time.ExperimentalTime

object SpreadManager {
    @ExperimentalTime
    fun spread(msg: SpreadMsg?) {
        if (msg == null) {
            return
        }
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }
}