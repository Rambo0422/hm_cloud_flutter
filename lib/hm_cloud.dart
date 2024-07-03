import 'package:flutter/services.dart';
import 'package:hm_cloud/hm_cloud_constants.dart';

/// 通信执行
class HmCloudController {
  late MethodChannel methodChannel;

  static late HmCloudController _instance;

  static HmCloudController get instance => _instance;

  HmCloudController() {
    methodChannel = const MethodChannel(HMCloudConstants.methodChannelName);
  }

  static void init() {
    _instance = HmCloudController();
  }

  void setCallback(platformCallHandler) {
    methodChannel.setMethodCallHandler(platformCallHandler);
  }

  /// 开始云游戏
  void startCloudGame(Map<String, dynamic> params) {
    methodChannel.invokeMethod(HMCloudConstants.startCloudGame, params);
  }

  Future<void> updateCloudGame(Map<String, dynamic> params) {
    return methodChannel.invokeMethod('updateGame', params);
  }
}
