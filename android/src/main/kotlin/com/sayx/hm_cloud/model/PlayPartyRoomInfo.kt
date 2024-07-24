package com.sayx.hm_cloud.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PlayPartyRoomInfo(
    @SerializedName("createTime")
    val createTime: Long = 0,
    @SerializedName("game")
    val game: Game = Game(),
    @SerializedName("game_id")
    val gameId: String = "",
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("people")
    val people: Int = 0,
    @SerializedName("residue")
    val residue: Int = 0,
    @SerializedName("room_id")
    val roomId: String = "",
    @SerializedName("room_status")
    val roomStatus: List<RoomStatu> = listOf(),
    @SerializedName("sort")
    val sort: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("type")
    val type: Int = 0,
    @SerializedName("uid")
    val uid: String = "",
    @SerializedName("user")
    val user: User = User()
) {
    @Keep
    data class Game(
        @SerializedName("cloud_game_id")
        val cloudGameId: String = "",
        @SerializedName("content1")
        val content1: String = "",
        @SerializedName("content2")
        val content2: String = "",
        @SerializedName("count1")
        val count1: Int = 0,
        @SerializedName("default_operation")
        val defaultOperation: Int = 0,
        @SerializedName("game_image_url")
        val gameImageUrl: String = "",
        @SerializedName("game_online")
        val gameOnline: Int = 0,
        @SerializedName("game_slogen")
        val gameSlogen: String = "",
        @SerializedName("_id")
        val id: String = "",
        @SerializedName("name")
        val name: String = "",
        @SerializedName("open_live")
        val openLive: Boolean = false,
        @SerializedName("room_count")
        val roomCount: Int = 0,
        @SerializedName("share_image_url")
        val shareImageUrl: String = ""
    )

    @Keep
    data class RoomStatu(
        @SerializedName("avatar_url")
        val avatarUrl: String = "",
        @SerializedName("index")
        val index: Int = 0,
        @SerializedName("nickname")
        val nickname: String = "",
        @SerializedName("status")
        val status: Int = 0,
        @SerializedName("uid")
        val uid: String = ""
    )

    @Keep
    data class User(
        @SerializedName("avatar_url")
        val avatarUrl: String = "",
        @SerializedName("_id")
        val id: String = ""
    )
}