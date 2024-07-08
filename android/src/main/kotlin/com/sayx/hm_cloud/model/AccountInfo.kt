package com.sayx.hm_cloud.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountInfo(
    val account: String?,
    @SerializedName("gameid")
    val gameId: String?,
    val password: String?,
    val platform: String?,
    @SerializedName("platform_game_id")
    val platformGameId: String?,
    @SerializedName("userid")
    val userId: String?,
    val key: String?,
    val token: String?,
) : Serializable {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}