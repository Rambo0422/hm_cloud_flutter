package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.blankj.utilcode.util.ScreenUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.PlayPartyRoomInfo

class PlayPartyGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val controlInfoViewList = mutableListOf<PlayPartyGameViewItem>()
    private val tvRoomId: TextView

    init {
        inflate(context, R.layout.view_play_party, this)
        findViewById<View>(R.id.layout_close_play_party).setOnClickListener {
            close()
        }

        controlInfoViewList.add(findViewById(R.id.play_party_item_1))
        controlInfoViewList.add(findViewById(R.id.play_party_item_2))
        controlInfoViewList.add(findViewById(R.id.play_party_item_3))
        controlInfoViewList.add(findViewById(R.id.play_party_item_4))

        controlInfoViewList.forEachIndexed { index, playPartyGameViewItem ->
            playPartyGameViewItem.setIndex(index + 1)
        }

        tvRoomId = findViewById(R.id.tv_room_id)
    }

    // 显示页面
    fun show() {
        val screenWidth = ScreenUtils.getScreenWidth().toFloat()
        val slideDownAnim = ObjectAnimator.ofFloat(this, "translationX", -screenWidth, 0f).apply {
            duration = 500
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    visibility = View.VISIBLE
                }
            })
        }
        slideDownAnim.start()
    }

    fun close() {
        val screenWidth = ScreenUtils.getScreenWidth().toFloat()
        val slideUpAnim = ObjectAnimator.ofFloat(this, "translationX", 0f, -screenWidth).apply {
            duration = 500
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    visibility = View.GONE
                }
            })
        }
        slideUpAnim.start()
    }

    fun onPlayPartyRoomInfoEvent(roomInfo: PlayPartyRoomInfo, controlInfos: List<ControlInfo>) {
        tvRoomId.text = "房间ID: ${roomInfo.roomId}"
        controlInfoViewList.forEachIndexed { index, playPartyGameViewItem ->
            val roomStatu = roomInfo.roomStatus[index]
            playPartyGameViewItem.onPlayPartyRoomInfoEvent(index, roomStatu, controlInfos)
        }
    }
}