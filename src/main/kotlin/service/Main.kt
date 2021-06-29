package service

import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main() {
    runBlocking {
        val propertiesReader = PropertiesReader.create()
        var service = Service(propertiesReader.serviceAddress, propertiesReader.servicePort)
        println("Gossip-8 module has been started.")
    }
}