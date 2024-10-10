package com.sayx.hm_cloud.model
import com.blankj.utilcode.util.LogUtils

data class GameParam(
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
    var accountInfo: Any?,
    var isPeakChannel: Boolean,
    var isPartyGame: Boolean,
    var specificArchive: SpecificArchive?,
    var custodian: String,
) {
    fun isVip(): Boolean {
        return vipExpiredTime > realTime
    }

    companion object {
        fun formGson(arguments: Map<*, *>): GameParam {
            return GameParam(
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
                arguments["accountInfo"],
                arguments["isPeakChannel"] as Boolean? ?: false,
                arguments["isPartyGame"] as Boolean? ?: false,
                getSpecificArchive(arguments["specificArchive"]),
                arguments["custodian"] as String? ?: "",
            )
        }

        private fun getSpecificArchive(data: Any?): SpecificArchive? {
            if (data is Map<*, *>) {
                return SpecificArchive().also {
                    it.gameId = data["gameId"] as String? ?: ""
                    val cid = data["cid"] as String?
                    it.cid = cid?.toLong() ?: 0L
                    it.md5 = data["fileMD5"] as String? ?: ""
                    it.downloadUrl = data["downLoadUrl"] as String? ?: ""
                    it.format = data["format"] as String? ?: ""
                    it.source = data["source"] as String? ?: ""
                }
            }
            return null
        }

        private fun getTimeValue(any: Any?): Long {
            if (any is Number) {
                return any.toLong()
            }
            return 0L
        }
    }
}
