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

  ///MARK: 初始化sdk
  void initSdk(Map<String, dynamic> params) {
    // print('init sdk $params');

    methodChannel.invokeMethod("initSDK", params);
  }

  ///MARK: 开始云游戏
  void startCloudGame(Map<String, dynamic> params) {
    methodChannel.invokeMethod(HMCloudConstants.startCloudGame, params);
  }

  void showController({int? operation}) {
    methodChannel.invokeMethod("showController", operation);
  }

  void setPCMouseMode(bool value) {
    methodChannel.invokeMethod("setPCMouseMode", value);
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

  void showToast(String msg) {
    methodChannel.invokeMethod("showToast", msg);
  }

  void controlPlay(params) {
    methodChannel.invokeMethod("controlPlay", params);
  }

  void closePage() {
    methodChannel.invokeMethod("closePage");
  }

  Future<void> updateCloudGame(Map<String, dynamic> params) {
    return methodChannel.invokeMethod('updateGame', params);
  }

  Future<void> queryControlUsers() {
    return methodChannel.invokeMethod('queryControlUsers', null);
  }

  void distributeControl(String controlInfos) {
    methodChannel.invokeMethod('distributeControl', controlInfos);
  }

  void testPage() {
    methodChannel.invokeMethod('test', null);
  }

  void sendPlaypartyInfo(Map<String, dynamic> params) {
    methodChannel.invokeMethod('playPartyInfo', params);
  }

  /// 游客向房主申请游玩权限
  void requestWantPlayPermission(Map<String, dynamic> wantPlayParams) {
    methodChannel.invokeMethod('requestWantPlayPermission', wantPlayParams);
  }

  Future<void> exitQueue() {
    return methodChannel.invokeMethod('exitQueue', null);
  }

  void exitGame() {
    methodChannel.invokeMethod('exitGame', null);
  }

  void updatePlayPartySoundAndMicrophoneState(Map<String, bool> params) {
    methodChannel.invokeMethod(
      'updatePlayPartySoundAndMicrophoneState',
      params,
    );
  }

// 购买成功通知
  void buySuccess() {
    methodChannel.invokeMethod('buySuccess', null);
  }

// 更新可玩时长，vip过期时间，高峰时长
  void updatePlayInfo(Map<String, dynamic> params) {
    methodChannel.invokeMethod('updatePlayInfo', params);
  }

  Future<dynamic> releaseGame(Map params) {
    return methodChannel.invokeMethod("releaseGame", params);
  }

  void getPinCode() {
    methodChannel.invokeMethod("getPinCode", null);
  }

  Future<dynamic> getUnReleaseGame() async {
    return await methodChannel.invokeMethod("getUnReleaseGame");
  }

  Future<dynamic> getArchiveProgress() async {
    return await methodChannel.invokeMethod("getArchiveProgress");
  }
}
