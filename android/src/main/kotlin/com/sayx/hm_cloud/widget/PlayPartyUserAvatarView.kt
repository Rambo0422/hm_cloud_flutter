package com.sayx.hm_cloud.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.PlayPartyRoomInfo

class PlayPartyUserAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val playPartyUserViewList = mutableListOf<PlayPartyUserAvatarItem>()

    init {
        inflate(context, R.layout.view_play_party_user, this)
        playPartyUserViewList.add(findViewById(R.id.play_party_user_item1))
        playPartyUserViewList.add(findViewById(R.id.play_party_user_item2))
        playPartyUserViewList.add(findViewById(R.id.play_party_user_item3))
        playPartyUserViewList.add(findViewById(R.id.play_party_user_item4))
    }

    fun setUserInfo(roomInfo: PlayPartyRoomInfo, controlInfos: List<ControlInfo>) {
        val roomStatus = roomInfo.roomStatus
        roomStatus.forEachIndexed { index, roomStatu ->
            val playPartyUserItem = playPartyUserViewList[index]
            val roomStatuUid = roomStatu.uid
            if (TextUtils.isEmpty(roomStatuUid)) {
                val isLock = roomStatu.status == 2
                playPartyUserItem.setRoomStatus(isLock)
            } else {
                // 该位置有人，判断是否有权限
                val hasPermission = (controlInfos.firstOrNull() {
                    it.uid == roomStatuUid
                }?.position ?: 0) > 0
                playPartyUserItem.setUserInfo(hasPermission, roomStatu)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        playPartyUserViewList.clear()
    }
}