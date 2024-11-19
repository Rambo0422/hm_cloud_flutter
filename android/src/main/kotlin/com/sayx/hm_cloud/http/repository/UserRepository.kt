package com.sayx.hm_cloud.http.repository

import com.sayx.hm_cloud.http.HttpManager
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.service.UserService
import com.sayx.hm_cloud.http.transformer.HttpError
import com.sayx.hm_cloud.http.transformer.RxSchedulers
import com.sayx.hm_cloud.model.ArchiveData
import io.reactivex.rxjava3.core.Observer

object UserRepository {

    private val userService: UserService = HttpManager.createApi(UserService::class.java)

    fun checkUserRecharge(observer: Observer<HttpResponse<Int>>) {
        userService.checkUserRecharge()
            .compose(HttpError.onError())
            .compose(RxSchedulers.schedulers())
            .subscribe(observer)
    }
}