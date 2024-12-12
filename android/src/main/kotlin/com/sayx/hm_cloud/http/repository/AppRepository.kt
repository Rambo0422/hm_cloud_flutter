package com.sayx.hm_cloud.http.repository

import com.sayx.hm_cloud.http.HttpManager
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.service.AppService
import com.sayx.hm_cloud.http.transformer.HttpError
import com.sayx.hm_cloud.http.transformer.RxSchedulers
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.GameConfig
import io.reactivex.rxjava3.core.Observer

object AppRepository {

    private val appService: AppService = HttpManager.createApi(AppService::class.java)

    fun requestArchiveData(params: HashMap<String, Any>, observer: Observer<HttpResponse<ArchiveData>>) {
        appService.requestArchiveData(params)
            .compose(HttpError.onError("https://archives.3ayx.net/getLast"))
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun requestGameConfig(observer: Observer<HttpResponse<GameConfig>>) {
        appService.requestGameConfig()
            .compose(HttpError.onError("/api/config/get?key=GAME_CONFIG"))
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }
}