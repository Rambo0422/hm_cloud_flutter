package com.example.hm_cloud

import androidx.lifecycle.Lifecycle

interface LifecycleProvider {
    fun getLifecycle(): Lifecycle?
}
