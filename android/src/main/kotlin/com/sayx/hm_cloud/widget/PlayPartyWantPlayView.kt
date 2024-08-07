package com.sayx.hm_cloud.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.blankj.utilcode.util.LogUtils
import com.noober.background.drawable.DrawableCreator
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import me.jessyan.autosize.utils.AutoSizeUtils

class PlayPartyWantPlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var textView: TextView

    init {
        initView()
    }

    private fun initView() {
        val layoutWidth = AutoSizeUtils.dp2px(context, 100f)
        val layoutHeight = AutoSizeUtils.dp2px(context, 40f)

        setDefaultBg()

        layoutParams = ConstraintLayout.LayoutParams(
            layoutWidth,
            layoutHeight
        ).apply {
            rightToRight = ConstraintSet.PARENT_ID
            bottomToBottom = ConstraintSet.PARENT_ID

            bottomMargin = AutoSizeUtils.dp2px(context, 20f)
            marginEnd = AutoSizeUtils.dp2px(context, 20f)
        }
        gravity = Gravity.CENTER

        setOnClickListener {
            // 判断我是不是房主，如果我是房主，就直接给权限，如果不是权限，就申请
            if (GameManager.isPartyPlayOwner) {
                GameManager.letPlay(GameManager.userId)
            } else {
                if (countDownTimer == null) {
                    GameManager.wantPlay(GameManager.userId)
                    // 开始30s倒计时
                    startCountDownTimer()
                }
            }
        }

        val imageView = ImageView(context).apply {
            val width = AutoSizeUtils.dp2px(context, 20f)
            layoutParams = LayoutParams(width, width)
            setImageResource(R.drawable.icon_play_party_want_play)
        }
        addView(imageView)

        textView = TextView(context).apply {
            text = "我要玩"
            setTextColor(Color.parseColor("#FF000000"))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSizeUtils.sp2px(context, 13f).toFloat())
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = AutoSizeUtils.dp2px(context, 5f)
            }
            setTypeface(typeface, Typeface.BOLD)
        }
        addView(textView)
    }

    private var countDownTimer: CountDownTimer? = null

    private fun startCountDownTimer() {
        if (countDownTimer == null) {
            background = DrawableCreator.Builder()
                .setCornersRadius(AutoSizeUtils.dp2px(context, 21f).toFloat())
                .setSolidColor(Color.parseColor("#FFCCCCCC"))
                .build()

            countDownTimer = object : CountDownTimer(30 * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val countDown = (millisUntilFinished / 1000).toString()
                    textView.text = "我要玩 $countDown"
                    textView.setTextColor(Color.parseColor("#FF000000"))
                }

                override fun onFinish() {
                    textView.text = "我要玩"
                    textView.setTextColor(Color.parseColor("#FF000000"))
                    setDefaultBg()
                    stopCountDown()
                }
            }
            countDownTimer?.start()
        }
    }

    fun stopCountDown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LogUtils.e("PlayPartyWantPlayView onDetachedFromWindow")
        stopCountDown()
    }

    private fun setDefaultBg() {
        background = DrawableCreator.Builder()
            .setCornersRadius(AutoSizeUtils.dp2px(context, 21f).toFloat())
            .setSolidColor(Color.parseColor("#FFC6EC4B"))
            .setStrokeColor(Color.parseColor("#FFA8C93E"))
            .setStrokeWidth(AutoSizeUtils.dp2px(context, 2f).toFloat())
            .build()
    }
}