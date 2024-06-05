package com.sayx.hm_cloud

import io.flutter.embedding.engine.plugins.FlutterPlugin

class HmCloudPlugin : FlutterPlugin {

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory(GameViewConstants.viewType, GameViewFactory(flutterPluginBinding.binaryMessenger))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }
}
