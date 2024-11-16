package com.sayx.hm_cloud.mvp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message

class GamePresenter(
    private val view: GameContract.IGameView,
) : GameContract.IGamePresenter {

    private var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 333) {
                getUserInfo()
            }
        }
    }
    private var availableTime: Long = 0  // 存储 availableTime

    override fun onCreate(context: Context) {
        getUserInfo()
    }

    override fun onDestroy() {
        // 销毁时取消定时器，避免内存泄漏
        handler.removeCallbacksAndMessages(null)
    }

    private fun getUserInfo() {
        view.getUserInfo()
    }

    // 处理从视图获取到的用户信息并更新 availableTime
    override fun onUserInfoReceived(availableTime: Long) {
        this.availableTime = availableTime

        // 根据 availableTime 动态调整请求间隔
        val interval = if (availableTime > 600000) { // availableTime > 10分钟（600000 毫秒）
            7 * 60 * 1000  // 5分钟
        } else {
            1 * 60 * 1000  // 1分钟
        }
        handler.sendEmptyMessageDelayed(333, interval.toLong())
    }
}