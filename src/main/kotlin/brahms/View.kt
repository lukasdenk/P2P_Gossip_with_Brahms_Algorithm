package brahms

import peers.Peer

class View(private val alpha: Float, private val beta: Float, private val gamma: Float, private val cache_size: Int) {
    val history = History(cache_size)
    val v: Set<Peer> = HashSet()
    val vPush: Set<Peer> = HashSet()
    val vPull: Set<Peer> = HashSet()

    private fun update() {
        while (true) {

        }
    }

}