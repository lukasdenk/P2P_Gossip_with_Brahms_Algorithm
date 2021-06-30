package service

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val propertiesReader = PreferencesReader.create(args.getOrElse(1) { "" })
        val service = Service(propertiesReader.serviceAddress, propertiesReader.servicePort)
        service.start()
        println("Gossip-8 service has been started at" +
                " ${propertiesReader.serviceAddress}:${propertiesReader.servicePort}")
        while (true) {
            delay(Duration.seconds(10))
        }
    }
}