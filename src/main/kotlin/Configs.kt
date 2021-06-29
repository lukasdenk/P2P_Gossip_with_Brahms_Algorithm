import peers.Peer
import java.util.*

class Configs {
    val self: Peer

    //    TODO: find good value
    val difficulty: Int = 4
    val cacheSize: Int

    private constructor() {

    }

    init {
//        TODO: read from INI-File
        self = Peer(UUID.randomUUID(), "test", "test")
        cacheSize = 1
    }


}