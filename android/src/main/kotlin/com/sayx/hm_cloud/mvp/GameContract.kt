package com.sayx.hm_cloud.mvp

import android.content.Context

class GameContract {

    interface IGamePresenter {
        fun onCreate(context: Context)

        fun onDestroy()

        fun onUserInfoReceived(availableTime: Long)
    }

    interface IGameView {
        fun getUserInfo(cache: Int)
    }
}