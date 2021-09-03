package p2p.brahms

import kotlinx.coroutines.delay
import main.Configs
import main.compareTo
import main.sha256
import messaging.p2p.Peer
import p2p.brahms.manager.ProbeManager
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class Sampler {
    var peer: AtomicReference<Peer?> = AtomicReference(null)
    var rand = AtomicReference(Random.nextBytes(32))
    var peerHash: AtomicReference<ByteArray?> = AtomicReference(null)


    fun initialize() {
//        TODO: sophisticated value
        rand.set(Random.nextBytes(32))
        peer.set(null)
        peerHash.set(null)
    }

    fun next(other: Peer) {
        val otherHash = (other.ip + rand.get()).toByteArray(Charset.forName("utf-8")).sha256()

        synchronized(this) {
            if (otherHash < peerHash.get()) {
                peer.set(other)
                peerHash.set(otherHash)
            }
        }
    }

    fun get(): Peer? {
        initializeIfOffline()
        return peer.get()
    }

    private fun initializeIfOffline() {
        if (peer.get()?.online == false) {
            initialize()
        }
    }

    //    TODO: call at beginning
    suspend fun probe() {
        while (true) {
            delay(Configs.probeInterval)

            val peerInstance = peer.get()
            initializeIfOffline()
            if (peerInstance != null) {
                ProbeManager.probe(peerInstance)
            }
        }
    }


}

