object JsonMapper {
//    fun mapToJson(msg: P2PMessage): String {
//        val body = Json.encodeToString(msg)
//        return Json.encodeToString(P2PCoreMsg(P2PMsgType.PULL_RESPONSE,body))
//    }
//
//    fun mapFromJson(json: String):P2PMessage{
//        val coreMsg = Json.decodeFromString<P2PCoreMsg>(json)
////        when(coreMsg.type){
//////            TODO: distinguish
////
////        }
//        return Json.decodeFromString<PullResponse>(coreMsg.jsonBody)
//    }
}