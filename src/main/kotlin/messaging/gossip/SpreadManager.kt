package messaging.gossip

import Configs
import brahms.View
import brahms.messaging.P2PCommunicator
import brahms.messaging.messages.SpreadMsg

object SpreadManager {
    fun spread(msg: SpreadMsg?) {
        if (msg == null) {
            return
        }
        for (peer in View.view) {
            P2PCommunicator.send(msg.copy(sender = Configs.getConfigs().self, receiver = peer))
        }
    }
}