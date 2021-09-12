package p2p.brahms

import main.Preferences
import main.sha256
import main.startsWithXLeadingZeroes
import messaging.p2p.Peer
import java.nio.charset.Charset

object PoW {
    fun work(receiver: Peer): Long {
        val timestamp = System.currentTimeMillis()
        var hash: ByteArray
        var nonce = -1L
        do {
            nonce++
            hash = buildPoW(timestamp, Preferences.self, receiver, nonce)
        } while (!hash.startsWithXLeadingZeroes(Preferences.difficulty))
        return nonce
    }

    fun buildPoW(timestamp: Long, sender: Peer, receiver: Peer, nonce: Long): ByteArray {
        val l = timestamp / 60000L
        return (l.toString() + sender.ip + receiver.ip + nonce).toByteArray(Charset.forName("utf-8"))
            .sha256()
    }
}