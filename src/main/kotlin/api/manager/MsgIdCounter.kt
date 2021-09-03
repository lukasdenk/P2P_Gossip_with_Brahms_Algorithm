package api.manager

import messaging.api.MsgId

object MsgIdCounter {
    var id: MsgId = 0

    @Synchronized
    fun increment(): MsgId {
        if (id == MsgId.MAX_VALUE) {
            id = 0
        } else {
            id++
        }
        return id
    }
}