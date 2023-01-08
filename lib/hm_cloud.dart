import 'package:flutter/services.dart';
import 'hm_cloud_platform_interface.dart';

typedef OnRecvChannelCallback = void Function(
  String actionName, {
  dynamic params,
});

class HmCloud {
  static final HmCloud _instance = HmCloud();

  static HmCloud get instance => _instance;

  Future<String?> getPlatformVersion() {
    return HmCloudPlatform.instance.getPlatformVersion();
  }

  Future<String?> getBatteryLevel() {
    return HmCloudPlatform.instance.getBatteryLevel();
  }
}

class HmCloudController {
  MethodChannel? methodChannel;

  HmCloudController() {
    methodChannel = const MethodChannel('hm_cloud_controller');
    methodChannel!.setMethodCallHandler(platformCallHandler);
  }

  static final HmCloudController _instance = HmCloudController();

  static HmCloudController get instance => _instance;

  OnRecvChannelCallback? callbak;

  setCallback(OnRecvChannelCallback callback) {
    callbak = callback;
  }

  Future<void> startCloudGame() {
    return methodChannel!.invokeMethod('startCloudGame');
  }

  Future<void> stopGame() {
    return methodChannel!.invokeMethod('stopGame');
  }

  Future<void> fullCloudGame(bool isFull) {
    return methodChannel!.invokeMethod('fullCloudGame', {'isFull': isFull});
  }

  ///实现监听原生方法回调
  Future<dynamic> platformCallHandler(MethodCall call) async {
    if (callbak != null) {
      callbak!(call.method, params: call.arguments);
    }
  }
}
