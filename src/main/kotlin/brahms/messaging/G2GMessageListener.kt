package brahms.messaging

import brahms.messaging.messages.G2GMessage

interface G2GMessageListener {
    fun receive(message: G2GMessage)
}