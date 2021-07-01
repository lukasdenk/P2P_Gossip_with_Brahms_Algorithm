import peers.Peer

class Configs private constructor() {
    val self: Peer

    //    TODO: find good value
    val difficulty: Int = 4
    val cacheSize: Int
    val probeTimeout = 4
    val probeInterval = 5L
    val kickInterval = 5L

    init {
//        TODO: read from INI-File
        self = Peer(ByteArray(32), "test", "k")
        cacheSize = 1
    }

    companion object {
        fun getConfigs(): Configs {
            return Configs()
        }
    }


}