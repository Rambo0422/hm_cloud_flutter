package com.sayx.hm_cloud

import android.content.Context
import com.sayx.hm_cloud.utils.LogUtils
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class GameViewFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        LogUtils.logD("GameViewFactory create GameView")
        return GameView(context, viewId, messenger, args)
    }
}