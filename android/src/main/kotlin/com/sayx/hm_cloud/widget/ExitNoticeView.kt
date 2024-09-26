package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.databinding.ViewExitNoticeBinding

class ExitNoticeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val dataBinding: ViewExitNoticeBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_exit_notice, this, true)

    private var isShow = false

    init {
        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
        dataBinding.btnClose.setOnClickListener {
            hideBoard()
        }
    }

    private fun showBoard() {
        isShow = true
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            -dataBinding.root.height.toFloat(),
            0.0f
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerImp() {
            override fun onAnimationStart(animation: Animator) {
                visibility = VISIBLE
            }
        })
        animator.start()
        postDelayed({
            if (isShow) {
                hideBoard()
            }
        }, 10 * 1000L)
    }

    private fun hideBoard() {
        isShow = false
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            0.0f,
            -dataBinding.root.height.toFloat()
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerImp() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                this@ExitNoticeView.parent?.let {
                    if (it is ViewGroup) {
                        it.removeView(this@ExitNoticeView)
                    }
                }
            }
        })
        animator.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            showBoard()
        }
    }
}