package p2p.brahms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import main.Preferences
import main.compareTo
import main.sha256
import messaging.p2p.Peer
import networking.service.ServicesManager
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@ExperimentalTime
class Sampler() {
    var peer: AtomicReference<Peer?> = AtomicReference(null)
    var rand = AtomicReference(Random.nextBytes(32))
    var peerHash: AtomicReference<ByteArray?> = AtomicReference(null)

    init {
        probe()
    }

    fun initialize() {
//        TODO: sophisticated value
        rand.set(Random.nextBytes(32))
        peer.set(null)
        peerHash.set(null)
    }

    fun next(other: Peer) {
        val otherHash = (other.ip + other.port + rand.get()).toByteArray(Charset.forName("utf-8")).sha256()

        synchronized(this) {
            if (otherHash < peerHash.get()) {
                peer.set(other)
                peerHash.set(otherHash)
            }
        }
    }

    fun get(): Peer? {
        return peer.get()
    }

    @ExperimentalTime
    fun probe() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(Preferences.probeInterval)
                val peerInstance = peer.get()
                if (peerInstance != null && !ServicesManager.isP2PPeerOnline(peerInstance)) {
                    initialize()
                }
            }
        }
    }


}

