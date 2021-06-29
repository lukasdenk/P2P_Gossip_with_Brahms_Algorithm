package service

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    runBlocking {
        val propertiesReader = PropertiesReader.create()
        val service = Service(propertiesReader.serviceAddress, propertiesReader.servicePort)
        service.start()
        println("Gossip-8 service has been started at" +
                " ${propertiesReader.serviceAddress}:${propertiesReader.servicePort}")
        while (true) {
            delay(Duration.seconds(10))
        }
    }
}