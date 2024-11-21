package com.sayx.hm_cloud.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.noober.background.drawable.DrawableCreator
import com.noober.background.view.BLFrameLayout
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.PlayPartyRoomInfo
import de.hdodenhof.circleimageview.CircleImageView
import me.jessyan.autosize.utils.AutoSizeUtils

class PlayPartyUserAvatarItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivPermissionTag: ImageView
    private val ivAvatar: CircleImageView
    private val ivRoomStatus: ImageView
    private val layoutCenter: BLFrameLayout

    init {
        inflate(context, R.layout.item_play_party_user, this)
        ivPermissionTag = findViewById(R.id.iv_permission_tag)
        ivAvatar = findViewById(R.id.iv_avatar)
        ivRoomStatus = findViewById(R.id.iv_room_status)
        layoutCenter = findViewById(R.id.layout_center)
        setRoomStatus(false)
    }

    fun setPermissionTag(hasPermission: Boolean) {
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

    fun setUserInfo(roomStatu: PlayPartyRoomInfo.RoomStatu) {
        ivRoomStatus.visibility = View.INVISIBLE
        ivAvatar.visibility = View.VISIBLE

        // 设置头像
        Glide.with(this)
            .load(roomStatu.avatarUrl)
            .placeholder(R.drawable.ic_play_party_avatal)
            .error(R.drawable.ic_play_party_avatal)
            .into(ivAvatar)

        val uid = roomStatu.uid
        if (uid == GameManager.getGameParam()?.userId) {
            layoutCenter.background = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#40000000"))
                .setStrokeColor(Color.parseColor("#FFC6EC4B"))
                .setStrokeWidth(AutoSizeUtils.dp2px(context, 3f).toFloat())
                .setShape(DrawableCreator.Shape.Oval)
                .build()
        } else {
            layoutCenter.background = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#40000000"))
                .setStrokeColor(Color.parseColor("#40FFFFFF"))
                .setStrokeWidth(AutoSizeUtils.dp2px(context, 3f).toFloat())
                .setShape(DrawableCreator.Shape.Oval)
                .build()
        }
    }
}