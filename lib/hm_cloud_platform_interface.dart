import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'hm_cloud_method_channel.dart';

abstract class HmCloudPlatform extends PlatformInterface {
  /// Constructs a HmCloudPlatform.
  HmCloudPlatform() : super(token: _token);

  static final Object _token = Object();

  static HmCloudPlatform _instance = MethodChannelHmCloud();

  /// The default instance of [HmCloudPlatform] to use.
  ///
  /// Defaults to [MethodChannelHmCloud].
  static HmCloudPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [HmCloudPlatform] when
  /// they register themselves.
  static set instance(HmCloudPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
