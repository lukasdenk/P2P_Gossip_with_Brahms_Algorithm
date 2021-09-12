package api.manager

import main.Preferences
import messaging.api.MsgId
import messaging.p2p.SpreadMsg
import java.util.*

/**
 * Thread-Safe message cache
 */
object MsgCache {
    private val msgCache: MutableMap<MsgId, Pair<SpreadMsg, Long>> = HashMap()
    private val msgTimeMillis: SortedMap<Long, MsgId> = TreeMap()

    @Synchronized
    fun put(id: MsgId, msg: SpreadMsg) {
        val timeMillis = System.currentTimeMillis()
        msgCache[id] = Pair(msg, timeMillis)
        msgTimeMillis[timeMillis] = id
        if (msgCache.size > Preferences.cacheSize) {
            val minTimesMillis = msgTimeMillis.firstKey()
            if (minTimesMillis != null) {
                val minId = msgTimeMillis.remove(minTimesMillis)
                msgCache.remove(minId)
            }
        }
    }

    @Synchronized
    fun remove(msgId: MsgId): SpreadMsg? {
        val pair = msgCache.remove(msgId)
        if (pair != null) {
            msgTimeMillis.remove(pair.second)
            return pair.first
        }
        return null
    }
}