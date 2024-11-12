package com.sayx.hm_cloud.http.service

import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.model.GameConfig
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

interface UserService {

    @GET("/api/user/isrecharge")
    fun checkUserRecharge() : Observable<HttpResponse<Int>>
}