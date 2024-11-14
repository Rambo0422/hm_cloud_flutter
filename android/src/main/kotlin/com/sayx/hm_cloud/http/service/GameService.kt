package com.sayx.hm_cloud.http.service

import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.model.ControllerInfo
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

interface GameService {

    @GET()
    fun getDefaultKeyboard(): Observable<HttpResponse<ControllerInfo>>
}