package messaging.api


abstract class APIMsg {
    abstract fun toByteArray(): ByteArray
}