
import 'hm_cloud_platform_interface.dart';

class HmCloud {
  Future<String?> getPlatformVersion() {
    return HmCloudPlatform.instance.getPlatformVersion();
  }
}
