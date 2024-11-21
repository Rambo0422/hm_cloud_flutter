package com.sayx.hm_cloud.model

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ControllerInfo(
    @SerializedName(value = "_id")
    @Expose(serialize = true) var id: String,
    @SerializedName(value = "type")
    @Expose(serialize = true) val type: Int,
    @SerializedName(value = "user_id")
    @Expose(serialize = true) var userId: String,
    @SerializedName(value = "game_id")
    @Expose(serialize = true) var gameId: String,
    @SerializedName(value = "keyboard")
    @Expose(serialize = true) var keyboard: List<KeyInfo>,
    @SerializedName(value = "name")
    @Expose(serialize = true) var name : String? = "",
    @SerializedName(value = "use")
    @Expose(serialize = true) var use : Int? = 0,
    @Expose(serialize = false) var isOfficial : Boolean? = false,
) : Serializable {

    override fun toString(): String {
        val gson = GsonBuilder()
            // 仅序列化有 @Expose 标记的字段
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        return gson.toJson(this)
    }
}
