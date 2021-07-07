package messaging.gossip

import brahms.View
import brahms.messaging.P2PCommunicator
import brahms.messaging.messages.SpreadMsg

object SpreadManager {
    fun spread(msg: SpreadMsg?) {
        if (msg == null) {
            return
        }
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }
}