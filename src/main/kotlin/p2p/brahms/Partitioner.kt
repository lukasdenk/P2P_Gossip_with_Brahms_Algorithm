package p2p.brahms

import main.Configs

object Partitioner {
    val alpha = 0.3
    val beta = 0.5
    val gamma = 0.2
    private val cacheSize = Configs.cacheSize

    val pushSize = (alpha * cacheSize).toInt()
    val pullSize = (beta * cacheSize).toInt()
    val historySize = cacheSize - pullSize - pushSize

}