package brahms

import compareTo
import peers.Peer
import sha256
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class Sampler {
    var peer: Peer? = null
    var rand = AtomicReference(ByteArray(32))
    var peerHash = ByteArray(32)

    fun initialize() {
//        TODO: sophisticated value
        rand.set(Random.nextBytes(32))
        this.peer = null
    }

    fun next(other: Peer) {
        val otherHash = (other.hostkey + rand.get()).sha256()

        synchronized(this) {
            if (otherHash < peerHash) {
                peer = other
                peerHash = otherHash
            }
        }
    }

    fun get(): Peer? {
        return peer
    }


}

