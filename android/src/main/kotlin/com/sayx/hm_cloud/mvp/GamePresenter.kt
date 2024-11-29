package com.sayx.hm_cloud.mvp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message

class GamePresenter(
    private val view: GameContract.IGameView,
) : GameContract.IGamePresenter {

    private var availableTime: Long = 0  // 存储 availableTime

    private var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 333) {
                getUserInfo(1)
            }
        }
    }

    override fun onCreate(context: Context) {
        getUserInfo(0)
    }

    override fun onDestroy() {
        // 销毁时取消定时器，避免内存泄漏
        handler.removeCallbacksAndMessages(null)
    }

    private fun getUserInfo(cache: Int) {
        view.getUserInfo(cache)
    }

    // 处理从视图获取到的用户信息并更新 availableTime
    override fun onUserInfoReceived(availableTime: Long) {
        this.availableTime = availableTime

        // 根据 availableTime 动态调整请求间隔
        val interval = if (availableTime > 60 * 12) { // availableTime > 12分钟
            1 * 60 * 1000  // 1分钟
        } else {
            1 * 60 * 1000  // 1分钟
        }
        handler.sendEmptyMessageDelayed(333, interval.toLong())
    }
}