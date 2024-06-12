package com.sayx.hm_cloud.model

import com.google.gson.annotations.SerializedName

data class AccountInfo(
    @SerializedName("account")
    val account: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("platform_game_id")
    val platformGameId: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("userid")
    val userId: String,
    @SerializedName("gameid")
    val gameId: String
)