package com.sayx.hm_cloud.model

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ControllerInfo(
    @Expose(serialize = true) val keyboard: List<KeyInfo>,
    @SerializedName(value = "user_id")
    @Expose(serialize = true) val userId: String,
    @SerializedName(value = "_id")
    @Expose(serialize = true) val id: String,
    @Expose(serialize = true) val type: Int,
    @SerializedName(value = "game_id")
    @Expose(serialize = true) val gameId: String
) : Serializable {

    companion object {
        fun fromData(data: Map<*, *>) : ControllerInfo{
            val keyboard = if (data["keyboard"] is ArrayList<*>) {
                (data["keyboard"] as ArrayList<*>).map { item->
                    KeyInfo.fromData(item)
                }.toList()
            } else {
                listOf()
            }
            val userId = if (data["user_id"] is String) {
                data["user_id"]
            } else {
                ""
            }
            val id = if (data["_id"] is String) {
                data["_id"]
            } else {
                ""
            }
            val type = if (data["type"] is Number) {
                (data["type"] as Number).toInt()
            } else {
                0
            }
            val gameId = if (data["game_id"] is String) {
                data["game_id"]
            } else {
                ""
            }
            return ControllerInfo(keyboard, userId as String, id as String, type, gameId as String)
        }
    }

    override fun toString(): String {
        val gson = GsonBuilder()
            // 仅序列化有 @Expose 标记的字段
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        return gson.toJson(this)
    }
}
