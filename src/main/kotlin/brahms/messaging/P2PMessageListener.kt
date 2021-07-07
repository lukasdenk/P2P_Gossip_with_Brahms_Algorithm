package brahms.messaging

import brahms.messaging.messages.P2PMessage

interface P2PMessageListener {
    fun receive(msg: P2PMessage)
}