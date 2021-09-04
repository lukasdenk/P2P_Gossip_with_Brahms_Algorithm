package p2p.brahms

import main.Preferences
import main.sha256
import main.startsWithXLeadingZeroes
import messaging.p2p.Peer
import java.nio.charset.Charset

object PoW {
    fun work(peer: Peer): Long {
        val timestamp = System.currentTimeMillis()
        var hash: ByteArray
        var nonce = 0L
        do {
            hash = buildPoW(timestamp, peer, nonce)
            nonce++
        } while (!hash.startsWithXLeadingZeroes(Preferences.difficulty))
        return nonce
    }

    fun buildPoW(timestamp: Long, peer: Peer, i: Long): ByteArray =
        ((timestamp / 60000L).toString() + peer.ip + Preferences.self.ip + i).toByteArray(Charset.forName("utf-8"))
            .sha256()
}