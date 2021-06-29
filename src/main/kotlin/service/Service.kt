package service

import java.net.InetSocketAddress
import java.net.SocketAddress

class Service(
    address: String,
    port: Int
) {
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
}