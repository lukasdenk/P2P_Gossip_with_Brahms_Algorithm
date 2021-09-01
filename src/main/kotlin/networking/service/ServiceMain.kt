package networking.service

import kotlinx.coroutines.runBlocking
import utils.MessageParser
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        val service = Service(
            address = propertiesReader.serviceAddress,
            port = propertiesReader.servicePort,
            read = { data: ByteBuffer, writer: (ByteArray) -> Unit ->
                println("Received message of type: ${MessageParser().toApiMessage(data).javaClass.name}")
                if (data.capacity() > 0) {
                    // TODO to write something in response use writer
                    //  writer.invoke("qwerty".toByteArray())
                }
            }
        )
        service.start()
    }
}