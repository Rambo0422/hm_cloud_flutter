package com.sayx.hm_cloud.http.repository

import android.content.res.AssetManager
import android.text.TextUtils
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.http.HttpManager
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.service.GameService
import com.sayx.hm_cloud.http.transformer.HttpError
import com.sayx.hm_cloud.http.transformer.RxSchedulers
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.KeyboardList
import io.reactivex.rxjava3.core.Observer
import java.io.InputStream

object GameRepository {

    private val gameService: GameService = HttpManager.createApi(GameService::class.java)

    fun requestDefaultGamepad(gameId: String?, observer: Observer<HttpResponse<ControllerInfo>>) {
        if (!TextUtils.isEmpty(gameId)) {
            gameService.requestDefaultKeyboard("1", gameId!!)
                .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/getdefault&type=1&game_id=$gameId"))
                .compose(RxSchedulers.schedulers())
                .subscribe(observer)
        } else {
            ToastUtils.showLong(R.string.request_keyboard_config_fail)
        }
    }

    fun requestDefaultKeyboard(gameId: String?, observer: Observer<HttpResponse<ControllerInfo>>) {
        if (!TextUtils.isEmpty(gameId)) {
            gameService.requestDefaultKeyboard("2", gameId!!)
                .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/getdefault&type=2&game_id=$gameId"))
                .compose(RxSchedulers.schedulers())
                .subscribe(observer)
        } else {
            ToastUtils.showLong(R.string.request_keyboard_config_fail)
        }
    }

    fun requestUserGamepadData(gameId: String?, observer: Observer<HttpResponse<KeyboardList>>) {
        if (!TextUtils.isEmpty(gameId)) {
            gameService.requestUserKeyboardData("1", 1, 3, gameId!!)
                .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/get&type=1&page=1&size=3&game_id=$gameId"))
                .compose(RxSchedulers.schedulers())
                .subscribe(observer)
        } else {
            ToastUtils.showLong(R.string.request_keyboard_config_fail)
        }
    }

    fun requestUserKeyboardData(gameId: String?, observer: Observer<HttpResponse<KeyboardList>>) {
        if (!TextUtils.isEmpty(gameId)) {
            gameService.requestUserKeyboardData("2", 1, 3, gameId!!)
                .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/get&type=2&page=1&size=3&game_id=$gameId"))
                .compose(RxSchedulers.schedulers())
                .subscribe(observer)
        } else {
            ToastUtils.showLong(R.string.request_keyboard_config_fail)
        }
    }

    fun requestAddKeyboard(keyboardInfo: ControllerInfo, observer: Observer<HttpResponse<String>>) {
        val params = hashMapOf(
            "name" to (keyboardInfo.name ?: ""),
            "game_id" to keyboardInfo.gameId,
            "type" to keyboardInfo.type,
            "keyboard" to keyboardInfo.keyboard.map { keyInfo -> keyInfo.toMap() }.toList()
        )
        gameService.requestAddKeyboard(params)
            .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/create"))
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun requestDeleteKeyboard(keyboardId: String, observer: Observer<HttpResponse<Any>>) {
        gameService.requestDeleteKeyboard(hashMapOf("id" to keyboardId))
            .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/del"))
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun requestUpdateKeyboard(keyboardInfo: ControllerInfo, observer: Observer<HttpResponse<Any>>) {
        val params = hashMapOf(
            "id" to keyboardInfo.id,
            "use" to (keyboardInfo.use ?: 0),
            "keyboard" to keyboardInfo.keyboard.map { keyInfo -> keyInfo.toMap() }.toList()
        )
        if (keyboardInfo.isOfficial != true) {
            params["name"] = keyboardInfo.name ?: ""
        }
        gameService.requestUpdateKeyboard(params)
            .compose(HttpError.onError("https://api-cgfc.3ayx.net/api/cloudgame/v2/keyboard/update"))
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun readDefaultGamepadFromAsset(assets: AssetManager?): ControllerInfo? {
        var controllerInfo: ControllerInfo? = null
        var inputStream : InputStream? = null
        try {
            inputStream = assets?.open("gamepad.json")
            val size = inputStream?.available()
            val buffer = size?.let { ByteArray(it) }
            inputStream?.read(buffer)
            inputStream?.close()
            val json = buffer?.let { String(it, Charsets.UTF_8) }
            if (!TextUtils.isEmpty(json)) {
                controllerInfo = GameManager.gson.fromJson(json!!, ControllerInfo::class.java)
            }
        } catch (e : Exception) {
            e.printStackTrace()
            inputStream?.close()
        }
        return controllerInfo
    }
}