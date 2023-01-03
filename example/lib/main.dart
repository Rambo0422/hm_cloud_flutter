import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:hm_cloud/hm_cloud.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _platformBatteryLevel = 'Unknown';
  final _hmCloudPlugin = HmCloud();

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initBatteryLevel();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _hmCloudPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> initBatteryLevel() async {
    String batteryLevel;
    try {
      batteryLevel = await _hmCloudPlugin.getBatteryLevel() ??
          'Unknown platform batteryLevel';
    } on PlatformException {
      batteryLevel = 'Failed to get platform batteryLevel.';
    }
    if (!mounted) {
      return;
    }

    setState(() {
      _platformBatteryLevel = batteryLevel;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              const SizedBox(
                height: 20,
              ),
              Text('Running on: $_platformBatteryLevel\n'),
            ],
          ),
        ),
      ),
    );
  }
}
