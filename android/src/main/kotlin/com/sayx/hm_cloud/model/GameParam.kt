package com.sayx.hm_cloud.model

data class GameParam(
    var accessKeyId: String,
    var gamePkName: String,
    var cToken: String,
    var userToken: String,
    var playTime: Long,
    var priority: Int,
    var userId: String,
    var channelName: String,
    var channel: String,
    var isVip: Boolean,
    var mute: Boolean,
//    var gameName: String,
    var gameId: String,
    var accountInfo: String?
//    var vipTime: Int
) {
    companion object {
        fun formGson(arguments: Map<*, *>): GameParam {
            val any = arguments["playTime"]
            var time = 0L
            if (any is Int) {
                time = any.toLong()
            }
            if (any is Long) {
                time = any
            }
            return GameParam(
                arguments["accessKeyId"] as String? ?: "",
                arguments["gamePkName"] as String? ?: "",
                arguments["cToken"] as String? ?: "",
                arguments["userToken"] as String? ?: "",
                time,
                arguments["priority"] as Int? ?: 0,
                arguments["userId"] as String? ?: "",
                arguments["channelName"] as String? ?: "",
                arguments["channel"] as String? ?: "",
                arguments["isVip"] as Boolean? ?: false,
                arguments["mute"] as Boolean? ?: false,
                arguments["gameId"] as String? ?: "",
                arguments["accountInfo"] as String?
            )
        }
    }
}
