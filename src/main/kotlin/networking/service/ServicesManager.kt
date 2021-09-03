package networking.service

import api.manager.APIMessagesManager
import json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.APIMessage
import messaging.api.Port
import messaging.p2p.P2PMessage
import messaging.p2p.Peer
import p2p.P2PCommunicator
import utils.*
import java.net.SocketAddress
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object ServicesManager {
    private lateinit var apiService: Service
    private lateinit var p2pService: Service

    suspend fun startApiService(address: String, port: Int) {
        apiService = Service(
            address = address,
            port = port,
            read = { address: SocketAddress, data: ByteBuffer ->
                val apiMessage = MessageParser().toApiMessage(data)
                APIMessagesManager.receive(
                    apiMessage,
                    portFromSocketAddressAsInt(address)
                )
                println(
                    "Received message of type: ${apiMessage.javaClass.name} from " +
                            "${ipFromSocketAddress(socketAddress = address)}:" +
                            portFromSocketAddressAsString(socketAddress = address)
                )
            }
        )
        apiService.start()
    }

    suspend fun startP2PService(p2pAddress: String, p2pPort: Int) {
        p2pService = Service(
            address = p2pAddress,
            port = p2pPort,
            read = { address: SocketAddress, data: ByteBuffer ->
                val message = JsonMapper.mapFromJson(data.readRemaining())
                P2PCommunicator.receive(
                    message,
                    Peer(
                        ipFromSocketAddress(address),
                        portFromSocketAddressAsInt(address)
                    )
                )
                println(
                    "Received message of type: ${message.javaClass.name} from " +
                            "${ipFromSocketAddress(socketAddress = address)}:" +
                            portFromSocketAddressAsString(socketAddress = address)
                )
            }
        )
        p2pService.start()
    }

    fun sendApiMessage(msg: APIMessage, port: Port) {
        //        TODO: create addr from port
//        apiService.write(, msg.toByteArray())
    }

    fun sendP2PMessage(msg: P2PMessage, peer: Peer) {
        p2pService.write(peer.toSocketAddress(), JsonMapper.mapToJson(msg))
    }
}