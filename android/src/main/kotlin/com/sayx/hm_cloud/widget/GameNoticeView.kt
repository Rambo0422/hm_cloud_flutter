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
import com.sayx.hm_cloud.databinding.ViewGameNoticeBinding
import com.sayx.hm_cloud.model.GameNotice

class GameNoticeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val dataBinding: ViewGameNoticeBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_game_notice, this, true)

    fun setNoticeData(notice: GameNotice) {
        dataBinding.tvTitle.text = notice.title
        dataBinding.tvSubtitle.text = notice.des
    }
}