package json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messaging.p2p.P2PMessage
import java.nio.charset.Charset

object JsonMapper {
    fun mapToJson(msg: P2PMessage): ByteArray {
        return Json.encodeToString(msg).toByteArray(Charset.forName("utf-8"))
    }

    fun mapFromJson(raw: ByteArray): P2PMessage {
        return Json.decodeFromString(raw.toString(Charset.forName("utf-8")))
    }
}