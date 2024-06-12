import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:hm_cloud/hm_cloud_constants.dart';

/// 通信回调声明
typedef OnReceiveChannelCallback = void Function(
  String actionName, {
  dynamic params,
});

/// 通信执行
class HmCloudController {
  MethodChannel methodChannel = const MethodChannel(HMCloudConstants.methodChannelName);

  HmCloudController() {
    methodChannel.setMethodCallHandler(platformCallHandler);
  }

  static final HmCloudController _instance = HmCloudController();

  static HmCloudController get instance => _instance;

  OnReceiveChannelCallback? callback;

  setCallback(OnReceiveChannelCallback callback) {
    this.callback = callback;
  }

  /// 开始云游戏
  void startCloudGame(Map<String, dynamic> params) async {
    final result = await methodChannel.invokeMethod(HMCloudConstants.startCloudGame, params);
    log("startCloudGame:$result");
  }

  /// 结束云游戏
  void stopGame() async {
    final result = await methodChannel.invokeMethod(HMCloudConstants.stopGame);
    log("stopGame:$result");
  }

  Future<dynamic> checkPlayingGame() async {
    await methodChannel.invokeMethod('checkPlayingGame');
  }

  /// 发送按键时间
  void sendCustomKey({
    int? inputOp,
    int? inputState,
    int? value,
    int? posCursorX,
    int? posCursorY,
    int? posMouseX,
    int? posMouseY,
  }) async {
    if (kDebugMode) {
      debugPrint({
        "inputOp": inputOp,
        "inputState": inputState,
        "value": value,
        "posCursor_x": posCursorX,
        "posCursor_y": posCursorY,
        "posMouse_x": posMouseX,
        "posMouse_y": posMouseY,
      }.toString());
    }

    final result = await methodChannel.invokeMethod(HMCloudConstants.sendCustomKey, {
      "inputOp": inputOp,
      "inputState": inputState,
      "value": value,
      "posCursor_x": posCursorX,
      "posCursor_y": posCursorY,
      "posMouse_x": posMouseX,
      "posMouse_y": posMouseY,
    });
    log("sendCustomKey:$result");
  }

  void setMouseMode(int mode) {
    methodChannel.invokeMethod(HMCloudConstants.setMouseMode, {"mode": mode});
  }

  void setQuality(int quality) {
    methodChannel.invokeMethod("setQuality", {"quality": quality});
  }

  void setMouseSensitivity(double sensitivity) {
    methodChannel.invokeMethod(HMCloudConstants.setMouseSensitivity, {"sensitivity": sensitivity});
  }

  void showInput() {
    methodChannel.invokeMethod(HMCloudConstants.showInput, null);
  }

  void switchInteraction(bool value) {
    methodChannel.invokeMethod("switchInteraction", {"interaction": value});
  }

  void setMute(bool mute) {
    methodChannel.invokeMethod("setMute", {"mute": mute});
  }

  Future<void> fullCloudGame(bool isFull) {
    return methodChannel.invokeMethod('fullCloudGame', {'isFull': isFull});
  }

  Future<void> updateCloudGame(
    Map<String, dynamic> params,
  ) {
    return methodChannel.invokeMethod('updateGame', params);
  }

  ///实现监听原生方法回调
  Future<dynamic> platformCallHandler(MethodCall call) async {
    if (callback != null) {
      callback!(call.method, params: call.arguments);
    }
  }

  /// 无效，不用试了
  void getPinCode() {
    methodChannel.invokeMethod('getPinCode', null);
  }

  void queryControlUsers() {
    methodChannel.invokeMethod('queryControlUsers', null);
  }
}
