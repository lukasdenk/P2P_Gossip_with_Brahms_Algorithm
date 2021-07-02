package client

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import utils.ParametersReader
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        println("Gossip-8 client has been started")
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val client = Client(
            gossipAddress = parametersReader.gossipServiceAddress,
            gossipPort = parametersReader.gossipServicePort
        )
        client.start()
        while (client.up) {
            delay(Duration.seconds(10))
        }
    }
}