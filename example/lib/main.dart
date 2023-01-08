import 'dart:developer';

import 'package:flutter/material.dart';
import 'dart:async';

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
      DeviceOrientation.landscapeLeft, //全屏时旋转方向，左边
    ]);

    // SystemChrome.setEnabledSystemUIOverlays([]);
    WidgetsFlutterBinding.ensureInitialized(); // add this line
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.manual, overlays: []);

    // HmCloudController.instance.setCallback((actionName, {pragma}) {
    //   print('来自iOS的回调$actionName');
    // });
    HmCloudController.instance.setCallback((actionName, {params}) {
      print('来自iOS的回调$actionName');
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Center(
          child: Column(
            children: [
              MaterialButton(
                onPressed: (() {
                  HmCloudController.instance.startCloudGame();
                }),
                child: const Text('开始游戏'),
              ),
              const Expanded(
                  child: HmCloudView(
                accessKey: '8a7a7a623d25ee7a3c87f688287bd4ba',
                accessKeyId: 'b14605e9d68',
                channelId: 'luehu',
                userId: 'test123',
                gameId: 'com.tencent.tmgp.sgame',
                isPortrait: false,
                playTime: 1000000,
              )),
            ],
          ),
        ),
      ),
    );
  }
}
