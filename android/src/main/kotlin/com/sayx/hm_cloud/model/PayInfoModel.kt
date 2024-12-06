package com.sayx.hm_cloud.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PayInfoModel(
    @SerializedName("payInfo")
    val payInfo: List<PayInfo> = listOf(),
    @SerializedName("userInfo")
    val userInfo: UserInfo = UserInfo()
) {
    @Keep
    data class PayInfo(
        @SerializedName("_add_time_second")
        val addTimeSecond: Int = 0,
        @SerializedName("_add_vip_time")
        val addVipTime: Int = 0,
        @SerializedName("expiryTime")
        val expiryTime: Any = Any(),
        @SerializedName("_expiryt")
        val expiryt: Int = 0,
        @SerializedName("_expirytgame")
        val expirytgame: Int = 0,
        @SerializedName("game_id")
        val gameId: String = "",
        @SerializedName("gamecombo_id")
        val gamecomboId: String = "",
        @SerializedName("_id")
        val id: String = "",
        @SerializedName("name")
        val name: String = "",
        @SerializedName("old_price")
        val oldPrice: Double = 0.0,
        @SerializedName("price")
        val price: Double = 0.0,
        @SerializedName("sub_name")
        val subName: String = "",
        @SerializedName("_tmpt")
        val tmpt: Int = 0,
        @SerializedName("_type")
        val type: Int = 0
    ) {
        // 二维码
        var codeUrl = ""

        // 订单号
        var orderNo = ""
    }

    @Keep
    data class UserInfo(
        @SerializedName("availableTime")
        val availableTime: Int = 0,
        @SerializedName("avatar")
        val avatar: String = "",
        @SerializedName("nickName")
        val nickName: String = ""
    )
}