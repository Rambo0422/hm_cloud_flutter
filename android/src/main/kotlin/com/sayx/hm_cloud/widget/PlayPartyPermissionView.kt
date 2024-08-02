package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.PartyPlayWantPlay
import me.jessyan.autosize.utils.AutoSizeUtils

/**
 * 派对吧权限view
 */
@SuppressLint("ViewConstructor")
class PlayPartyPermissionView @JvmOverloads constructor(
    private val partyPlayWantPlay: PartyPlayWantPlay,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val ivUserAvatar: ImageView
    private val tvNickName: TextView
    private val tvMessage: TextView
    private val btnAgree: View
    private val btnClose: View
    private var countDownTimer: CountDownTimer? = null

    init {
        inflate(context, R.layout.view_play_party_permission, this)
        ivUserAvatar = findViewById(R.id.iv_user_avatar)
        tvNickName = findViewById(R.id.tv_nick_name)
        tvMessage = findViewById(R.id.tv_message)
        btnAgree = findViewById(R.id.btn_agree)
        btnClose = findViewById(R.id.btn_close)

        ClickUtils.applyGlobalDebouncing(btnClose) {
            close()
        }

        ClickUtils.applyGlobalDebouncing(btnAgree) {
            GameManager.letPlay(partyPlayWantPlay.uid)
            close()
        }

        setUserInfo()
    }

    // 显示页面
    fun show() {
        val height = AutoSizeUtils.dp2px(context, 20f).toFloat()
        val slideDownAnim = ObjectAnimator.ofFloat(this, "translationY", -height, 0f).apply {
            duration = 500
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    visibility = View.VISIBLE
                    startCountDown()
                }
            })
        }
        slideDownAnim.start()
    }

    fun close() {
        val height = AutoSizeUtils.dp2px(context, 20f).toFloat()
        val slideUpAnim = ObjectAnimator.ofFloat(this, "translationY", 0f, -height).apply {
            duration = 500
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    visibility = View.GONE
                    // 将自己移除
                    remove()
                }
            })
        }
        slideUpAnim.start()
    }

    fun remove() {
        if (parent != null) {
            (parent as ViewGroup).removeView(this)
        }
    }

    // 开始60秒倒计时
    fun startCountDown() {
        countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val countDown = (millisUntilFinished / 1000).toString()
                tvMessage.text = "申请游戏控制权(${countDown}s)"
            }

            override fun onFinish() {
                close()
            }
        }
        countDownTimer?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun setUserInfo() {
        // 显示头像，昵称，
        val nickName = partyPlayWantPlay.nickName
        if (nickName.isNotEmpty()) {
            tvNickName.text = nickName
        } else {
            val uid = partyPlayWantPlay.uid
            tvNickName.text = "*****${uid.substring(uid.length - 4)}"
        }

        val avatar = partyPlayWantPlay.avatar


        // 设置头像
        Glide.with(this)
            .load(avatar)
            .placeholder(R.drawable.ic_play_party_avatal)
            .error(R.drawable.ic_play_party_avatal)
            .into(ivUserAvatar)
    }
}