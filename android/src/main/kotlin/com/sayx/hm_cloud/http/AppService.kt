package com.sayx.hm_cloud.http

import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.model.ArchiveData
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface AppService {

    @POST("https://archives.3ayx.net/getLast")
    fun requestArchiveData(
        @Body body: HashMap<String, Any>
    ) : Observable<HttpResponse<ArchiveData>>
}