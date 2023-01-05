import 'package:flutter/services.dart';
import 'hm_cloud_platform_interface.dart';

class HmCloud {
  Future<String?> getPlatformVersion() {
    return HmCloudPlatform.instance.getPlatformVersion();
  }

  Future<String?> getBatteryLevel() {
    return HmCloudPlatform.instance.getBatteryLevel();
  }

  Future<void> startCloudGame() {
    return HmCloudController.instance.startCloudGame();
  }
}

class HmCloudController {
  final _methodChannel = const MethodChannel('hm_cloud_controller');

  static final HmCloudController _instance = HmCloudController();

  static HmCloudController get instance => _instance;

  Future<void> startCloudGame() {
    return _methodChannel.invokeMethod('startCloudGame');
  }
}
