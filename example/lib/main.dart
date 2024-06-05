import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hm_cloud/hm_cloud.dart';
import 'package:hm_cloud/hm_cloud_view.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool isFull = false;

  @override
  void initState() {
    super.initState();

    SystemChrome.setPreferredOrientations([
      // 强制横屏
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
    //
    // SystemChrome.setPreferredOrientations([
    //   DeviceOrientation.landscapeLeft, //全屏时旋转方向，左边
    // ]);
    //
    // // SystemChrome.setEnabledSystemUIOverlays([]);
    // WidgetsFlutterBinding.ensureInitialized(); // add this line
    // SystemChrome.setEnabledSystemUIMode(SystemUiMode.manual, overlays: []);
    //
    // // HmCloudController.instance.setCallback((actionName, {pragma}) {
    // //   print('来自iOS的回调$actionName');
    // // });
    // HmCloudController.instance.setCallback((actionName, {params}) {
    //   print('来自iOS的回调$actionName');
    // });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Stack(
          alignment: Alignment.center,
          children: [
            const SizedBox(
              width: double.infinity,
              height: double.infinity,
              child: HmCloudView(),
            ),
            MaterialButton(
              onPressed: () {
                var params = {
                  'token': 'cba578f98c3f6cef74a5c01b6eefab0b6af946f5',
                  'accessKeyId': '615a1227dc8',
                  'gameId': 'JustCause4',
                  'channelId': 'szlk',
                  'userId': '65d7ff1a09664cbba0722de6',
                  'expireTime': 2000000,
                  'userToken': '65d7ff1a09664cbba0722de6',
                  'priority': 48
                };

                HmCloudController.instance.startCloudGame(
                  params,
                );
              },
              child: const Text('跳转'),
            ),
            Align(
              alignment: Alignment.centerLeft,
              child: MaterialButton(
                onPressed: () {
                  HmCloudController.instance.sendCustomKey(
                    inputOp: 1024,
                    inputState: 1,
                    value: 4096,
                  );

                  Future.delayed(const Duration(seconds: 1), () {
                    HmCloudController.instance.sendCustomKey(
                      inputOp: 1024,
                      inputState: 1,
                      value: 0,
                    );
                  });
                },
                child: const Text('sendCustomkey'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
