import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'hm_cloud_platform_interface.dart';

/// An implementation of [HmCloudPlatform] that uses method channels.
class MethodChannelHmCloud extends HmCloudPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('hm_cloud');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
