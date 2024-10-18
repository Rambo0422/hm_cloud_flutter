package com.sayx.hm_cloud.http

import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.transformer.HttpError
import com.sayx.hm_cloud.http.transformer.RxSchedulers
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.GameConfig
import io.reactivex.rxjava3.core.Observer

class AppRepository {

    private val appService: AppService = HttpManager.createApi(AppService::class.java)

    fun requestArchiveData(params: HashMap<String, Any>, observer: Observer<HttpResponse<ArchiveData>>) {
        appService.requestArchiveData(params)
            .compose(HttpError.onError())
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }

    fun requestGameConfig(observer: Observer<HttpResponse<GameConfig>>) {
        appService.requestGameConfig()
            .compose(HttpError.onError())
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }
}