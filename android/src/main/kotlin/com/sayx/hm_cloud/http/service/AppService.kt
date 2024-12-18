package com.sayx.hm_cloud.http.service

import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.GameConfig
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AppService {

    @POST("https://archives.3ayx.net/getLast")
    fun requestArchiveData(
        @Body body: HashMap<String, Any>
    ) : Observable<HttpResponse<ArchiveData>>

    @GET("/api/config/get?key=GAME_CONFIG")
    fun requestGameConfig() : Observable<HttpResponse<GameConfig>>
}