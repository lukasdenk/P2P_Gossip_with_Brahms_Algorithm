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
class Sampler(peer: Peer? = null) {
    var atomicPeer: AtomicReference<Peer?>
    private var rand = AtomicReference(Random.nextBytes(32))
    var peerHash: AtomicReference<ByteArray?>

    init {
        atomicPeer = AtomicReference(peer)
        peerHash = if (peer == null) {
            AtomicReference()
        } else {
            AtomicReference(hashPeer(peer))
        }
        probe()
    }

    fun initialize() {
//        TODO: sophisticated value
        rand.set(Random.nextBytes(32))
        atomicPeer.set(null)
        peerHash.set(null)
    }

    fun next(other: Peer) {
        val otherHash = hashPeer(other)

        synchronized(this) {
            if (otherHash < peerHash.get()) {
                atomicPeer.set(other)
                peerHash.set(otherHash)
            }
        }
    }

    private fun hashPeer(other: Peer) =
        (other.ip + other.port + rand.get()).toByteArray(Charset.forName("utf-8")).sha256()

    fun get(): Peer? {
        return atomicPeer.get()
    }

    @ExperimentalTime
    fun probe() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(Preferences.probeInterval)
                val peerInstance = atomicPeer.get()
                if (peerInstance != null && !ServicesManager.isP2PPeerOnline(peerInstance)) {
                    println("peer ${peerInstance.port} went offline")
                    initialize()
                }
            }
        }
    }


}

