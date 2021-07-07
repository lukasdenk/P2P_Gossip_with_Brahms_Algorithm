package messaging.p2p.manager

import brahms.View
import messaging.p2p.messages.SpreadMsg
import messaging.p2p.P2PCommunicator

object SpreadManager {
    fun spread(msg: SpreadMsg?) {
        if (msg == null) {
            return
        }
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }
}