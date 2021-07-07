package brahms

import Configs
import brahms.messaging.ProbeManager
import compareTo
import kotlinx.coroutines.delay
import peers.Peer
import sha256
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

//    TODO: probe
fun next(other: Peer) {
    val otherHash = (other.hostkey + rand.get()).sha256()

    synchronized(this) {
        if (otherHash < peerHash.get()) {
            peer.set(other)
            peerHash.set(otherHash)
        }
    }
}

    fun get(): Peer? {
        if (peer.get()?.online == false) {
            initialize()
        }
        return peer.get()
    }

    //    TODO: call at beginning
    suspend fun probe() {
        while (true) {
            delay(Configs.getConfigs().probeInterval)

            val peerInstance = peer.get()
            if (peerInstance != null) {
                ProbeManager.probe(peerInstance)
            }
        }
    }


}

