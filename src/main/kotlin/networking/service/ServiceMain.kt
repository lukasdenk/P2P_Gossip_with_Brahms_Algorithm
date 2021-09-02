package networking.service

import api.manager.APIMessagesManager
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.p2p.Peer
import p2p.brahms.P2PMessagesManager
import utils.MessageParser
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

// TODO combine two main modules
@DelicateCoroutinesApi
@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        // TODO add method that can send messages to peers by port in service.
        //  Throw an exception if peer is not connected anymore.
        val apiService = Service(
            address = propertiesReader.gossipServiceAddress,
            port = propertiesReader.gossipServicePort,
            read = { data: ByteBuffer, writer: (ByteArray) -> Unit ->
                // TODO check if it is P2P or API message
                //  If this is P2P we call
                //  If this is API we call
                APIMessagesManager.receive(MessageParser().toApiMessage(data), 8080)// TODO pass the port to the result
                // P2PMessagesManager.receive(MessageParser().toPeerToPeerMessage(data), Peer()) // TODO add peer with ip and port
                println("Received message of type: ${MessageParser().toApiMessage(data).javaClass.name}")
                if (data.capacity() > 0) {
                    // TODO to write something in response use writer
                    //  writer.invoke("qwerty".toByteArray())
                }
            }
        )
        val p2pService = Service(
            address = propertiesReader.p2pServiceAddress,
            port = propertiesReader.p2pServicePort,
            read = { data: ByteBuffer, writer: (ByteArray) -> Unit ->
                // P2PMessagesManager.receive(MessageParser().toPeerToPeerMessage(data), Peer()) // TODO add peer with ip and port
                if (data.capacity() > 0) {
                    // TODO to write something in response use writer
                    //  writer.invoke("qwerty".toByteArray())
                }
            }
        )
        CoroutineScope(Dispatchers.IO).launch { apiService.start() }
        CoroutineScope(Dispatchers.IO).launch { p2pService.start() }.join()
    }
}