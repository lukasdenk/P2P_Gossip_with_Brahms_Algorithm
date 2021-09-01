package messaging.api


interface APIMessage {
    fun toByteArray(): ByteArray
}