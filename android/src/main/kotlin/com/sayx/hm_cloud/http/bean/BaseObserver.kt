package com.sayx.hm_cloud.http.bean

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

abstract class BaseObserver<T : Any>() : Observer<T> {

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(response: T) {
    }

    override fun onError(e: Throwable) {
    }

    override fun onComplete() {
    }
}