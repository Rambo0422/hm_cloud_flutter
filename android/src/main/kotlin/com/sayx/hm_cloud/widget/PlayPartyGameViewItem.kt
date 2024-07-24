package com.sayx.hm_cloud.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.noober.background.drawable.DrawableCreator
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.constants.PlayPartyPlayStatus
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.PlayPartyRoomInfo
import me.jessyan.autosize.utils.AutoSizeUtils
import org.w3c.dom.Text

class PlayPartyGameViewItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tv_user_index: TextView
    private val tv_user_name: TextView
    private val view_home_owner_tag: View
    private val iv_avatar: ImageView
    private val iv_lock_tag: ImageView
    private val iv_permission_tag: ImageView
    private val btn_play_status: TextView
    private val group_visitor: Group

    private val currentUid = "665eb60c1c90b65e435d3863"

    //    private val isPartyPlayOwner = GameManager.isPartyPlayOwner
    private val isPartyPlayOwner = true

    init {
        inflate(context, R.layout.view_play_party_item, this)

        tv_user_index = findViewById(R.id.tv_user_index)
        tv_user_name = findViewById(R.id.tv_user_name)
        view_home_owner_tag = findViewById(R.id.view_home_owner_tag)
        iv_avatar = findViewById(R.id.iv_avatar)
        iv_lock_tag = findViewById(R.id.iv_lock_tag)
        iv_permission_tag = findViewById(R.id.iv_permission_tag)
        btn_play_status = findViewById(R.id.btn_play_status)
        group_visitor = findViewById(R.id.group_visitor)
    }

    fun setIndex(index: Int) {
        tv_user_index.text = "${index}P"
    }

    fun onPlayPartyRoomInfoEvent(index: Int, roomStatu: PlayPartyRoomInfo.RoomStatu, controlInfos: List<ControlInfo>) {
        view_home_owner_tag.visibility = if (index == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }

        val uid = roomStatu.uid

        // 先判断当前坐席是否有人
        if (TextUtils.isEmpty(uid)) {
            // 说明当前坐席没人
            tv_user_name.text = "虚位以待"
            tv_user_name.setTextColor(Color.parseColor("#FF8995A9"))

            val status = roomStatu.status
            if (status == 2) {
                iv_lock_tag.setImageResource(R.drawable.ic_play_party_position_lock)
            } else {
                iv_lock_tag.setImageResource(R.drawable.ic_play_party_position_unlock)
            }

            iv_lock_tag.visibility = View.VISIBLE
            iv_avatar.visibility = View.GONE
        } else {
            setUserInfo(roomStatu)

            // 判断当前用户是否有权限
            val roomStatuUid = roomStatu.uid

            val hasPermission = (controlInfos.firstOrNull() {
                it.uid == roomStatuUid
            }?.position ?: 0) > 0

            // 判断当前item是否是自己
            var itemIsMy = roomStatuUid == currentUid

            if (isPartyPlayOwner) {
                if (hasPermission) {
                    iv_permission_tag.visibility = View.VISIBLE
                    setStatus(PlayPartyPlayStatus.WANT_PLAY)
                } else {
                    iv_permission_tag.visibility = View.GONE
                    setStatus(PlayPartyPlayStatus.NO_PERMISSION)
                }
            } else {
                if (hasPermission) {
                    iv_permission_tag.visibility = View.VISIBLE
                    setStatus(PlayPartyPlayStatus.HAS_PERMISSION)
                } else {
                    iv_permission_tag.visibility = View.GONE
                    setStatus(PlayPartyPlayStatus.NO_PERMISSION)
                }
            }
        }
    }

    private fun setStatus(status: PlayPartyPlayStatus) {
        when (status) {
            PlayPartyPlayStatus.NO_PERMISSION -> {
                if (isPartyPlayOwner) {
                    btn_play_status.visibility = GONE
                    group_visitor.visibility = VISIBLE
                } else {
                    btn_play_status.text = "让我玩"
                    btn_play_status.setTextColor(Color.parseColor("#FF000000"))
                    btn_play_status.background = setBg()
                    btn_play_status.visibility = VISIBLE
                }
            }


            PlayPartyPlayStatus.LET_ME_PLAY -> {

            }
            PlayPartyPlayStatus.LET_TA_PLAY -> {

            }

            PlayPartyPlayStatus.HAVE_PERMISSION -> {

            }
            PlayPartyPlayStatus.POSITION_LOCK -> {

            }
            PlayPartyPlayStatus.POSITION_OPEN -> {
            }
        }

    }

    private fun setUserInfo(roomStatu: PlayPartyRoomInfo.RoomStatu) {
        val roomStatuUid = roomStatu.uid

        // 先设置昵称
        val nickname = roomStatu.nickname
        if (TextUtils.isEmpty(nickname)) {
            tv_user_name.text = "*****${roomStatuUid.substring(roomStatuUid.length - 4)}"
        } else {
            tv_user_name.text = nickname
        }

        if (roomStatuUid == currentUid) {
            tv_user_name.setTextColor(Color.parseColor("#FFC6EC4B"))
        } else {
            tv_user_name.setTextColor(Color.parseColor("#FFFFFFFF"))
        }

        val avatarUrl = roomStatu.avatarUrl

        // 设置头像
        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_play_party_avatal)
            .error(R.drawable.ic_play_party_avatal)
            .into(iv_avatar)

        iv_avatar.visibility = View.VISIBLE
        iv_lock_tag.visibility = View.GONE
    }

    private fun setBg(): Drawable? {
        val radius = AutoSizeUtils.dp2px(context, 3f).toFloat()
        val builder = DrawableCreator.Builder()
            .setCornersRadius(radius)
        builder.setSolidColor(Color.parseColor("#FFC6EC4B"))
        return builder.build()
    }
}