package com.sayx.hm_cloud.http.service

import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.KeyboardList
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GameService {

    @GET("/api/cloudgame/v2/keyboard/getdefault")
    fun requestDefaultKeyboard(
        @Query("type") type: String,
        @Query("game_id") gameId: String,
    ): Observable<HttpResponse<ControllerInfo>>

    @GET("/api/cloudgame/v2/keyboard/get")
    fun requestUserKeyboardData(
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("game_id") gameId: String,
    ): Observable<HttpResponse<KeyboardList>>

    @POST("/api/cloudgame/v2/keyboard/create")
    fun requestAddKeyboard(
        @Body body: HashMap<String, Any>
    ) : Observable<HttpResponse<Any>>

    @POST("/api/cloudgame/v2/keyboard/del")
    fun requestDeleteKeyboard(
        @Body body: HashMap<String, String>
    ) : Observable<HttpResponse<Any>>

    @POST("/api/cloudgame/v2/keyboard/update")
    fun requestUpdateKeyboard(
        @Body body: HashMap<String, Any>
    ): Observable<HttpResponse<Any>>
}