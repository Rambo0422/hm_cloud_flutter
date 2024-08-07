package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ScreenUtils
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.PartyPlayWantPlay
import com.sayx.hm_cloud.model.PlayPartyRoomInfo
import me.jessyan.autosize.AutoSizeCompat
import org.json.JSONObject

class PlayPartyGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val controlInfoViewList = mutableListOf<PlayPartyGameViewItem>()
    private val tvRoomId: TextView
    private val ivPlayPartySound: ImageView
    private val ivPlayPartyMicrophone: ImageView
    private var soundState = false
    private var microphoneState = false

    init {
        inflate(context, R.layout.view_play_party, this)
        findViewById<View>(R.id.layout_close_play_party).setOnClickListener {
            close()
        }

        ivPlayPartySound = findViewById(R.id.iv_play_party_sound)
        ivPlayPartyMicrophone = findViewById(R.id.iv_play_party_microphone)

        findViewById<View>(R.id.btn_play_party_sound).setOnClickListener {
            val arguments = JSONObject().apply {
                put("sound", !soundState)
                put("microphone", microphoneState)
            }.toString()
            GameManager.setPlayPartySoundAndMicrophone(arguments)
        }

        findViewById<View>(R.id.btn_play_party_microphone).setOnClickListener {
            val arguments = JSONObject().apply {
                put("sound", soundState)
                put("microphone", !microphoneState)
            }.toString()
            GameManager.setPlayPartySoundAndMicrophone(arguments)
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
            if (controlInfos.isEmpty()) {
                // 因为 controlInfos 是空的，所以第一个默认有权限
                if (index == 0) {
                    playPartyGameViewItem.setPermission(true)
                }
            }
        }
    }

    fun onPartyPlayWantPlay(partyPlayWantPlay: PartyPlayWantPlay) {
        for (playPartyGameViewItem in controlInfoViewList) {
            val uid = playPartyGameViewItem.getUserId()
            if (uid == partyPlayWantPlay.uid) {
                playPartyGameViewItem.startCountDown()
                break
            }
        }
    }

    fun setSoundAndMicrophoneState(soundState: Boolean, microphoneState: Boolean) {
        this.soundState = soundState
        this.microphoneState = microphoneState

        if (soundState) {
            ivPlayPartySound.setImageResource(R.drawable.ic_play_party_sound)
        } else {
            ivPlayPartySound.setImageResource(R.drawable.ic_play_party_sound_close)
        }

        if (microphoneState) {
            ivPlayPartyMicrophone.setImageResource(R.drawable.ic_play_party_microphone)
        } else {
            ivPlayPartyMicrophone.setImageResource(R.drawable.ic_play_party_microphone_close)
        }
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        AutoSizeCompat.autoConvertDensity(resources, 812f, false)
        return super.generateLayoutParams(lp)
    }
}