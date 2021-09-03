package json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messaging.p2p.P2PMessage
import java.nio.charset.Charset

object JsonMapper {
    fun mapToJson(msg: P2PMessage): ByteArray {
        val string = Json.encodeToString(msg)
        return string.toByteArray(Charset.forName("utf-8"))
    }

    fun mapFromJson(raw: ByteArray): P2PMessage {
        val string = raw.toString(Charset.forName("utf-8"))
        return Json.decodeFromString(string)
    }
}