import 'package:flutter_test/flutter_test.dart';
import 'package:hm_cloud/hm_cloud.dart';
import 'package:hm_cloud/hm_cloud_platform_interface.dart';
import 'package:hm_cloud/hm_cloud_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockHmCloudPlatform
    with MockPlatformInterfaceMixin
    implements HmCloudPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final HmCloudPlatform initialPlatform = HmCloudPlatform.instance;

  test('$MethodChannelHmCloud is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelHmCloud>());
  });

  test('getPlatformVersion', () async {
    HmCloud hmCloudPlugin = HmCloud();
    MockHmCloudPlatform fakePlatform = MockHmCloudPlatform();
    HmCloudPlatform.instance = fakePlatform;

    expect(await hmCloudPlugin.getPlatformVersion(), '42');
  });
}
