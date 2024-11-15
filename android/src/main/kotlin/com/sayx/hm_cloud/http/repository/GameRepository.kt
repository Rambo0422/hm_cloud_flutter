package com.sayx.hm_cloud.http.repository

import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.http.HttpManager
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.service.GameService
import com.sayx.hm_cloud.http.transformer.HttpError
import com.sayx.hm_cloud.http.transformer.RxSchedulers
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.KeyboardList
import io.reactivex.rxjava3.core.Observer

object GameRepository {

    private val gameService: GameService = HttpManager.createApi(GameService::class.java)

    fun requestDefaultGamepad(observer: Observer<HttpResponse<ControllerInfo>>) {
        gameService.requestDefaultGamepad("1")
            .compose(HttpError.onError())
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun requestDefaultKeyboard(gameId: String?, observer: Observer<HttpResponse<ControllerInfo>>) {
        gameId?.let {
            gameService.requestDefaultKeyboard("2", it)
                .compose(HttpError.onError())
                .compose(RxSchedulers.schedulers())
                .subscribe(observer)
        } ?: ToastUtils.showLong(R.string.request_keyboard_config_fail)
    }

    fun requestUserGamepadData(observer: Observer<HttpResponse<KeyboardList>>) {
        gameService.requestUserGamepadData("1", 1, 4)
            .compose(HttpError.onError())
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun requestUserKeyboardData(gameId: String?, observer: Observer<HttpResponse<KeyboardList>>) {
        gameId?.let {
            gameService.requestUserKeyboardData("2", 1, 4, it)
                .compose(HttpError.onError())
                .compose(RxSchedulers.schedulers())
                .subscribe(observer)
        } ?: ToastUtils.showLong(R.string.request_keyboard_config_fail)
    }
}