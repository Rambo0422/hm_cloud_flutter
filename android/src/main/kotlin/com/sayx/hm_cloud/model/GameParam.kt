package com.sayx.hm_cloud.model

import com.sayx.hm_cloud.utils.TimeUtils

data class GameParam(
    // 密钥id
    var channel: String,
    // 密钥id
    var accessKeyId: String,
    // 游戏包名
    var gamePkName: String,
    // 游戏名
    var gameName: String,
    // 实例token
    var cToken: String,
    // 用户token
    var userToken: String,
    // 用户可玩时长
    var playTime: Long,
    var peakTime: Long,
    var realTime: Long,
    // 队列等级
    var priority: Int,
    // 用户id
    var userId: String,
    // 游戏channel
    var channelName: String,
    var vipExpiredTime: Long,
    var gameId: String,
    var cid: String,
    var accountInfo: Any?,
    var isPeakChannel: Boolean,
    var isPartyGame: Boolean,
    var gameType: String,
    // 默认按键类型 1:键盘，2:手柄
    var defaultOperation: Int,
    // 支持按键类型 1:键盘，2:手柄，3:两者
    var supportOperation: Int,
) {
    fun isVip(): Boolean {
        return vipExpiredTime > TimeUtils.currentTime()
    }

    override fun toString(): String {
        return "GameParam(" +
                "channel='$channel', " +
                "accessKeyId='$accessKeyId', " +
                "gamePkName='$gamePkName', " +
                "gameName='$gameName', " +
                "cToken='$cToken', " +
                "userToken='$userToken', " +
                "playTime=$playTime, " +
                "peakTime=$peakTime, " +
                "realTime=$realTime, " +
                "priority=$priority, " +
                "userId='$userId', " +
                "channelName='$channelName', " +
                "vipExpiredTime=$vipExpiredTime, " +
                "gameId='$gameId', " +
                "cid='$cid', " +
                "accountInfo=$accountInfo, " +
                "isPeakChannel=$isPeakChannel, " +
                "isPartyGame=$isPartyGame," +
                "gameType=$gameType," +
                "defaultOperation=$defaultOperation," +
                "supportOperation=$supportOperation," +
                ")"
    }

    companion object {
        fun formGson(arguments: Map<*, *>): GameParam {
            return GameParam(
                arguments["channel"] as String? ?: "",
                arguments["accessKeyId"] as String? ?: "",
                arguments["gamePkName"] as String? ?: "",
                arguments["gameName"] as String? ?: "",
                arguments["cToken"] as String? ?: "",
                arguments["userToken"] as String? ?: "",
                getTimeValue(arguments["playTime"]),
                getTimeValue(arguments["peakTime"]),
                getTimeValue(arguments["realTime"]),
                (arguments["priority"] as Number?)?.toInt() ?: 0,
                arguments["userId"] as String? ?: "",
                arguments["channelName"] as String? ?: "",
                getTimeValue(arguments["vipExpiredTime"]),
                arguments["gameId"] as String? ?: "",
                arguments["cid"] as String? ?: "",
                arguments["accountInfo"],
                arguments["isPeakChannel"] as Boolean? ?: false,
                arguments["isPartyGame"] as Boolean? ?: false,
                arguments["gameType"] as String? ?: "",
                (arguments["defaultOperation"] as Number?)?.toInt() ?: 2,
                (arguments["supportOperation"] as Number?)?.toInt() ?: 3,
            )
        }

        fun getTimeValue(any: Any?): Long {
            if (any is Number) {
                return any.toLong()
            }
            return 0L
        }
    }
}
