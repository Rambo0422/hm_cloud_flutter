import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hm_cloud/hm_cloud_method_channel.dart';

void main() {
  MethodChannelHmCloud platform = MethodChannelHmCloud();
  const MethodChannel channel = MethodChannel('hm_cloud');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
