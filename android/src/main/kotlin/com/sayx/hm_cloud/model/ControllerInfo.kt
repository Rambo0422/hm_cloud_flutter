package com.sayx.hm_cloud.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sayx.hm_cloud.constants.GameConstants
import java.io.Serializable

data class ControllerInfo(
    val keyboard: List<KeyInfo>,
    @SerializedName(value = "user_id")
    val userId: String,
    @SerializedName(value = "_id")
    val id: String,
    val type: Int,
    @SerializedName(value = "game_id")
    val gameId: String
) : Serializable {
    fun isDefault(): Boolean {
        return userId == GameConstants.defaultControllerUserId
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
