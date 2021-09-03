package utils

import java.net.SocketAddress
import java.nio.ByteBuffer

fun ByteBuffer.readRemaining(): ByteArray {
    val remaining = ByteArray(this.remaining())
    var byteArrayPosition = 0
    while (this.hasRemaining()) {
        remaining[byteArrayPosition++] = this.get()
    }
    return remaining
}

fun ByteBuffer.toByteArray(): ByteArray {
    val result = ByteArray(this.capacity())
    this.position(0)
    this.get(result, 0, result.size)
    return result
}

fun ByteArray.toReadableString(): String {
    return this.map(Byte::toInt).joinToString(" ") { String.format("%02X", it) }
}

fun ipFromSocketAddress(socketAddress: SocketAddress): String {
    val address = socketAddress.toString().replace("/", "")
    return address.substring(0, address.indexOf(":"))
}

fun portFromSocketAddressAsString(socketAddress: SocketAddress): String {
    val address = socketAddress.toString().replace("\\", "")
    return address.substring(address.indexOf(":") + 1)
}

fun portFromSocketAddressAsInt(socketAddress: SocketAddress): Int {
    return Integer.parseInt(portFromSocketAddressAsString(socketAddress = socketAddress))
}