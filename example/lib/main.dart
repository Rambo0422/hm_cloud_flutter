import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hm_cloud/hm_cloud.dart';

void main() {
  HmCloudController.init();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  void _incrementCounter() {
    // 跳转游戏横屏
    HmCloudController.instance.testPage();
  }

  @override
  void initState() {
    super.initState();
    // 每隔2s，发送一次json
    _startTimer();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            TextButton(
              onPressed: () {
                loadJson();
              },
              child: const Text("获取json"),
            )
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }

  Timer? _timer;

  void _startTimer() {
    _timer = Timer.periodic(const Duration(seconds: 2), (timer) {
      loadJson();
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void loadJson() async {
    final String controlInfo =
        await rootBundle.loadString('assets/control_info.json');
    final String roomInfo =
        await rootBundle.loadString('assets/room_info.json');
    var params = {
      "controlInfos": controlInfo,
      "roomInfo": roomInfo,
    };
    HmCloudController.instance.sendPlaypartyInfo(params);
  }
}
