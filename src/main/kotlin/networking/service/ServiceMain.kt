package networking.service

import api.manager.APIMessagesManager
import kotlinx.coroutines.runBlocking
import messaging.p2p.P2PMessage
import messaging.p2p.Peer
import p2p.brahms.P2PMessagesManager
import utils.MessageParser
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

// TODO combine two main modules
@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        // TODO add method that can send messages to peers by port in service.
        //  Throw an exception if peer is not connected anymore.
        val service = Service(
            address = propertiesReader.serviceAddress,
            port = propertiesReader.servicePort,
            read = { data: ByteBuffer, writer: (ByteArray) -> Unit ->
                // TODO check if it is P2P or API message
                //  If this is P2P we call
                //  If this is API we call
                APIMessagesManager.receive(MessageParser().toApiMessage(data), 8080)// TODO pass the port to the result
                // P2PMessagesManager.receive(P2PMessage(), Peer()) // TODO add p2p messages parsing, and add peer with ip and port
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