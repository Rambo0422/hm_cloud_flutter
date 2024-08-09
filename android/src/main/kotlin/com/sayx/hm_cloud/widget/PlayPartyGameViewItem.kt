package com.sayx.hm_cloud.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.bumptech.glide.Glide
import com.noober.background.drawable.DrawableCreator
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.constants.PlayPartyPlayStatus
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.PlayPartyRoomInfo
import de.hdodenhof.circleimageview.CircleImageView
import me.jessyan.autosize.utils.AutoSizeUtils

class PlayPartyGameViewItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tv_user_index: TextView
    private val tv_user_name: TextView
    private val view_home_owner_tag: View
    private val iv_avatar: CircleImageView
    private val iv_lock_tag: ImageView
    private val iv_permission_tag: ImageView
    private val btn_play_status: TextView
    private val group_visitor: Group
    private val btn_let_play: View
    private val layout_avatar: FrameLayout
    private val tvWantPlayCountDown: TextView
    private val btnKickOut: TextView

    private var currentUid = ""
    private var isPartyPlayOwner = false

    private var status: PlayPartyPlayStatus? = null
    private var roomStatu: PlayPartyRoomInfo.RoomStatu? = null

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
        btn_let_play = findViewById(R.id.btn_let_play)
        layout_avatar = findViewById(R.id.layout_avatar)
        tvWantPlayCountDown = findViewById(R.id.tv_want_play_count_down)
        btnKickOut = findViewById(R.id.btn_kick_out)

        btn_let_play.setOnClickListener {
            GameManager.letPlay(roomStatu?.uid ?: "")
        }

        btnKickOut.setOnClickListener {
            GameManager.kickOutUser(roomStatu?.uid ?: "")
        }

        btn_play_status.setOnClickListener {
            when (status) {
                PlayPartyPlayStatus.HAVE_PERMISSION -> {
                    GameManager.closeUserPlay(roomStatu?.uid ?: "")
                }

                PlayPartyPlayStatus.NO_PERMISSION -> {
                    GameManager.letPlay(roomStatu?.uid ?: "")
                }

                PlayPartyPlayStatus.POSITION_LOCK -> {
                    val position = roomStatu?.index ?: -1
                    if (position != -1) {
                        GameManager.changePositionStatus(position, false)
                    }
                }

                PlayPartyPlayStatus.POSITION_OPEN -> {
                    // 打开位置
                    val position = roomStatu?.index ?: -1
                    if (position != -1) {
                        GameManager.changePositionStatus(position, true)
                    }
                }

                PlayPartyPlayStatus.LET_ME_PLAY -> {
                    GameManager.wantPlay(roomStatu?.uid ?: "")
                }

                PlayPartyPlayStatus.LET_TA_PLAY -> {
                    GameManager.letPlay(roomStatu?.uid ?: "")
                }

                else -> {}
            }
        }
    }

    fun setIndex(index: Int) {
        tv_user_index.text = "${index}P"
    }

    fun onPlayPartyRoomInfoEvent(index: Int, roomStatu: PlayPartyRoomInfo.RoomStatu, controlInfos: List<ControlInfo>) {
        this.roomStatu = roomStatu
        currentUid = GameManager.userId
        isPartyPlayOwner = GameManager.isPartyPlayOwner
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

            if (isPartyPlayOwner) {
                if (status == 2) {
                    setStatus(PlayPartyPlayStatus.POSITION_LOCK)
                } else {
                    setStatus(PlayPartyPlayStatus.POSITION_OPEN)
                }
            }

            iv_lock_tag.visibility = View.VISIBLE
            iv_avatar.visibility = View.GONE
            iv_permission_tag.visibility = View.GONE
            stopCountDown()
            setLayoutAvatarBg(false)
        } else {
            setUserInfo(roomStatu)

            // 判断当前用户是否有权限
            val roomStatuUid = roomStatu.uid

            val hasPermission = (controlInfos.firstOrNull() {
                it.uid == roomStatuUid
            }?.position ?: 0) > 0

            // 判断当前item是否是自己
            val itemIsMy = roomStatuUid == currentUid

            if (isPartyPlayOwner) {
                if (hasPermission) {
                    setStatus(PlayPartyPlayStatus.HAVE_PERMISSION)
                } else {
                    if (itemIsMy) {
                        setStatus(PlayPartyPlayStatus.NO_PERMISSION)
                    } else {
                        group_visitor.visibility = View.VISIBLE
                        btn_play_status.visibility = View.GONE
                    }
                }
            } else {
                if (itemIsMy && !hasPermission) {
                    setStatus(PlayPartyPlayStatus.LET_ME_PLAY)
                } else {
                    group_visitor.visibility = View.GONE
                    btn_play_status.visibility = View.GONE
                }
            }

            if (hasPermission) {
                iv_permission_tag.visibility = View.VISIBLE
                // 当前有权限就停止计时
                stopCountDown()
            } else {
                iv_permission_tag.visibility = View.GONE
            }
        }
    }

    private fun setStatus(status: PlayPartyPlayStatus) {
        this.status = status
        when (status) {
            PlayPartyPlayStatus.NO_PERMISSION -> {
                btn_play_status.background = DrawableCreator.Builder()
                    .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
                    .setSolidColor(Color.parseColor("#FFC6EC4B"))
                    .build()
                btn_play_status.visibility = View.VISIBLE
                btn_play_status.text = "让Ta玩"
                btn_play_status.setTextColor(Color.parseColor("#FF000000"))
            }


            PlayPartyPlayStatus.LET_ME_PLAY -> {
                btn_play_status.background = DrawableCreator.Builder()
                    .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
                    .setSolidColor(Color.parseColor("#FFC6EC4B"))
                    .build()
                btn_play_status.visibility = View.VISIBLE
                btn_play_status.text = "让我玩"
                btn_play_status.setTextColor(Color.parseColor("#FF000000"))
            }

            PlayPartyPlayStatus.LET_TA_PLAY -> {
                btn_play_status.background = DrawableCreator.Builder()
                    .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
                    .setSolidColor(Color.parseColor("#FFC6EC4B"))
                    .build()
                btn_play_status.visibility = View.VISIBLE
                btn_play_status.text = "让Ta玩"
                btn_play_status.setTextColor(Color.parseColor("#FF000000"))
            }

            PlayPartyPlayStatus.HAVE_PERMISSION -> {
                btn_play_status.background = DrawableCreator.Builder()
                    .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
                    .setSolidColor(Color.parseColor("#FFC6EC4B"))
                    .build()
                btn_play_status.visibility = View.VISIBLE
                btn_play_status.text = "不让玩"
                btn_play_status.setTextColor(Color.parseColor("#FF000000"))
            }

            PlayPartyPlayStatus.POSITION_LOCK -> {
                btn_play_status.background = DrawableCreator.Builder()
                    .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
                    .setSolidColor(Color.parseColor("#FF222A3A"))
                    .build()
                btn_play_status.visibility = View.VISIBLE
                btn_play_status.text = "打开位置"
                btn_play_status.setTextColor(Color.parseColor("#FF9CA3B4"))
            }

            PlayPartyPlayStatus.POSITION_OPEN -> {
                btn_play_status.background = DrawableCreator.Builder()
                    .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
                    .setSolidColor(Color.parseColor("#FF222A3A"))
                    .build()
                btn_play_status.visibility = View.VISIBLE
                btn_play_status.text = "锁定位置"
                btn_play_status.setTextColor(Color.parseColor("#FF9CA3B4"))
            }
        }
        btn_play_status.visibility = View.VISIBLE
        group_visitor.visibility = View.GONE
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
            // 是自己，头像边框设置颜色
            iv_avatar.borderColor = Color.parseColor("#FFC6EC4B")
        } else {
            tv_user_name.setTextColor(Color.parseColor("#FFFFFFFF"))
            iv_avatar.borderColor = Color.parseColor("#FFC6EC4B")
        }

        setLayoutAvatarBg(roomStatuUid == currentUid)

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

    private fun setLayoutAvatarBg(isSelect: Boolean) {
        context?.let { ctx ->
            kotlin.runCatching {
                val strokeColor = if (isSelect) "#FFC6EC4B" else "#FF434C5B"
                val drawable = createDrawable(ctx, strokeColor)
                layout_avatar.background = drawable
            }
        }
    }

    private fun createDrawable(context: Context, strokeColor: String): Drawable {
        return DrawableCreator.Builder()
            .setCornersRadius(AutoSizeUtils.dp2px(context, 4f).toFloat())
            .setStrokeColor(Color.parseColor(strokeColor))
            .setStrokeWidth(AutoSizeUtils.dp2px(context, 1f).toFloat())
            .setShape(DrawableCreator.Shape.Oval)
            .build()
    }

    private var countDownTimer: CountDownTimer? = null

    // 开始计时
    // 开始60秒倒计时
    fun startCountDown() {
        if (countDownTimer == null) {
            tvWantPlayCountDown.visibility = View.VISIBLE
            countDownTimer = object : CountDownTimer(30 * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val countDown = (millisUntilFinished / 1000).toString()
                    tvWantPlayCountDown.text = "我要玩(${countDown}s)"
                }

                override fun onFinish() {
                    stopCountDown()
                }
            }
            countDownTimer?.start()
        }
    }

    fun stopCountDown() {
        countDownTimer?.cancel()
        countDownTimer = null
        tvWantPlayCountDown.visibility = View.GONE
    }

    fun getUserId(): String {
        return roomStatu?.uid ?: ""
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopCountDown()
    }

    fun setPermission(permission: Boolean) {
        if (permission) {
            iv_permission_tag.visibility = View.VISIBLE
        } else {
            iv_permission_tag.visibility = View.GONE
        }
    }
}