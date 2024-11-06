import 'dart:collection';
import 'dart:developer';

import 'package:flutter/foundation.dart';
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

  void showController({int? operation}) {
    methodChannel.invokeMethod("showController", operation);
  }

  void setPCMouseMode() {
    methodChannel.invokeMethod("setPCMouseMode");
  }

  void setControllerData(dynamic data) {
    methodChannel.invokeMethod("setControllerData", data);
  }

  void onEditSuccess(params) {
    methodChannel.invokeMethod("controllerEditSuccess", params);
  }

  void onEditFail(params) {
    methodChannel.invokeMethod("controllerEditFail");
  }

  Future<void> updateCloudGame(Map<String, dynamic> params) {
    return methodChannel.invokeMethod('updateGame', params);
  }

  Future<void> queryControlUsers() {
    return methodChannel.invokeMethod('queryControlUsers', null);
  }

  Future<dynamic> checkUnReleaseGame(String userId, String gameId) {
    Map<String, dynamic> params = {
      "userId": userId,
      "gameId": gameId,
    };
    return methodChannel.invokeMethod("checkUnReleaseGame", params);
  }

  void releaseGame() {
    methodChannel.invokeMethod("releaseGame", null);
  }

  Future<dynamic> releaseOldGame(String userId) {
    return methodChannel.invokeMethod("releaseOldGame", userId);
  }

  Future<dynamic> leaveQueue() {
    return methodChannel.invokeMethod("leaveQueue", "");
  }

  Future<dynamic> getOldGameInfo(String userId) {
    Map<String, dynamic> params = {
      "userId": userId,
    };
    return methodChannel.invokeMethod("getOldGameInfo", params);
  }
}
