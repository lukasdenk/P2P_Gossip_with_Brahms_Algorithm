package p2p.manager

import messages.p2p.SpreadMsg
import p2p.P2PCommunicator
import p2p.brahms.View

object SpreadManager {
    fun spread(msg: SpreadMsg?) {
        if (msg == null) {
            return
        }
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }
}