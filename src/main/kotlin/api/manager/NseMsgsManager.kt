package api.manager

import api.APICommunicator
import kotlinx.coroutines.delay
import main.Configs
import messaging.api.APIMessage
import messaging.api.APIMessageListener
import messaging.api.nse.NseEstimate
import messaging.api.nse.NseQuery

object NseMsgsManager : APIMessageListener {
    var estimation = Configs.seed.size
    override fun receive(msg: APIMessage, sender: Int) {
        if (msg is NseEstimate) {
            estimation = msg.estimatePeers
        }
    }

    suspend fun estimate() {
        APICommunicator.send(NseQuery(), Configs.nseModule)
        delay(Configs.estimationInterval)
    }
}