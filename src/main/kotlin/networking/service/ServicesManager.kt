package networking.service

import api.manager.APIMessagesManager
import json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.APIMessage
import messaging.p2p.P2PMessage
import messaging.p2p.Peer
import p2p.P2PCommunicator
import utils.MessageParser
import utils.ipFromSocketAddress
import utils.portFromSocketAddressAsInt
import utils.portFromSocketAddressAsString
import java.net.SocketAddress
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object ServicesManager {
    private lateinit var apiService: Service
    private lateinit var p2pService: Service

    suspend fun startApiService(gossipAddress: String, port: Int) {
        apiService = Service(
            address = gossipAddress,
            port = port,
            read = { address: SocketAddress, data: ByteArray ->
                val apiMessage = MessageParser().toApiMessage(ByteBuffer.wrap(data))
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
            read = { address: SocketAddress, data: ByteArray ->
                val message = JsonMapper.mapFromJson(data)
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

    fun isP2PPeerOnline(peer: Peer): Boolean {
        return p2pService.isOnline(peer.toSocketAddress())
    }

    fun sendApiMessage(msg: APIMessage, port: Int) {
        //        TODO: create addr from port
//        apiService.write(, msg.toByteArray())
    }

    fun sendP2PMessage(msg: P2PMessage, peer: Peer) {
        p2pService.write(peer.toSocketAddress(), JsonMapper.mapToJson(msg))
    }
}