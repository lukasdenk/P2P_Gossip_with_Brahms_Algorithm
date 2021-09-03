package p2p

import json.SpreadMsg
import p2p.brahms.View

object SpreadManager {
    fun spread(msg: SpreadMsg?) {
        if (msg == null) {
            return
        }
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }
}