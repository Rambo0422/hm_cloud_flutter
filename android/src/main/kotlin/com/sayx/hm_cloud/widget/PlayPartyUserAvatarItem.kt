package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.PlayPartyRoomInfo
import de.hdodenhof.circleimageview.CircleImageView

class PlayPartyUserAvatarItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivPermissionTag: ImageView
    private val ivAvatar: CircleImageView
    private val ivRoomStatus: ImageView

    init {
        inflate(context, R.layout.item_play_party_user, this)
        ivPermissionTag = findViewById(R.id.iv_permission_tag)
        ivAvatar = findViewById(R.id.iv_avatar)
        ivRoomStatus = findViewById(R.id.iv_room_status)
        setRoomStatus(false)
    }

    private fun setPermissionTag(hasPermission: Boolean) {
        if (hasPermission) {
            ivPermissionTag.visibility = View.VISIBLE
        } else {
            ivPermissionTag.visibility = View.INVISIBLE
        }
    }

    fun setRoomStatus(isLock: Boolean) {
        if (isLock) {
            ivRoomStatus.setImageResource(R.drawable.ic_play_party_position_lock)
        } else {
            ivRoomStatus.setImageResource(R.drawable.ic_play_party_position_unlock)
        }

        if (ivRoomStatus.visibility != View.VISIBLE) {
            ivRoomStatus.visibility = View.VISIBLE
        }
        ivAvatar.visibility = View.GONE
        setPermissionTag(false)
    }

    fun setUserInfo(hasPermission: Boolean, roomStatu: PlayPartyRoomInfo.RoomStatu) {
        ivRoomStatus.visibility = View.INVISIBLE
        ivAvatar.visibility = View.VISIBLE

        // 设置头像
        Glide.with(this)
            .load(roomStatu.avatarUrl)
            .placeholder(R.drawable.ic_play_party_avatal)
            .error(R.drawable.ic_play_party_avatal)
            .into(ivAvatar)

        setPermissionTag(hasPermission)
    }
}