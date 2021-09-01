package utils

import java.nio.ByteBuffer

fun ByteBuffer.readRemaining(): ByteArray {
    val remaining = ByteArray(this.remaining())
    var byteArrayPosition = 0
    while (this.hasRemaining()) {
        remaining[byteArrayPosition++] = this.get()
    }
    return remaining
}