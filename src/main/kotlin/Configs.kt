import peers.Peer

class Configs private constructor() {
    val self: Peer

    //    TODO: find good value
    val difficulty: Int = 4
    val cacheSize: Int

    init {
//        TODO: read from INI-File
        self = Peer("test", "test", "k")
        cacheSize = 1
    }

    companion object {
        fun getConfigs(): Configs {
            return Configs()
        }
    }


}