package service

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import utils.ParametersReader
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        val service = Service(
            propertiesReader.serviceAddress,
            propertiesReader.servicePort,
            read = { data: ByteArray, writer: (ByteArray) -> Unit ->
                if (data.isNotEmpty()) {
                    // TODO to write something in response use writer
                    //  writer.invoke("qwerty".toByteArray())
                }
            }
        )
        service.start()
        println("Gossip-8 service has been started at" +
                " ${propertiesReader.serviceAddress}:${propertiesReader.servicePort}")
        while (true) {
            delay(Duration.seconds(10))
        }
    }
}