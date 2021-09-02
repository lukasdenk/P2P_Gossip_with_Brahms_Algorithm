package p2p.brahms

import main.Configs

object Bootstrapper {

    //    TODO: in initializer class
    fun bootstrap() {
        View.view = Configs.seed
    }
}