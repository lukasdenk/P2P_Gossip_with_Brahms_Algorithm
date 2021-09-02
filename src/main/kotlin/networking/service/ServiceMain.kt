package networking.service

import api.manager.APIMessagesManager
import kotlinx.coroutines.runBlocking
import utils.MessageParser
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

// TODO combine two main modules
// TODO
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
                // TODO check if it is P2P or API message
                //  If this is P2P we call
                //  If this is API we call
                APIMessagesManager.receive(MessageParser().toApiMessage(data), 8080)// TODO pass the port to the result
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