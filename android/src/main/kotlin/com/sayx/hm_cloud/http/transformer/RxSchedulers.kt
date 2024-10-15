package com.sayx.hm_cloud.http.transformer

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * RxSchedulers
 */
object RxSchedulers {

    fun <T : Any> schedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T : Any> syncSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
        }
    }
}