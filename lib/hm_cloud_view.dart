import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

class HmCloudView extends StatefulWidget {
  // View 是否横屏或者竖屏
  final bool isPortrait;
  final String extraInfo;
  final int playTime;

  const HmCloudView({
    super.key,
    required this.accessKey,
    required this.accessKeyId,
    required this.gameId,
    required this.channelId,
    required this.userId,
    this.isPortrait = false,
    this.extraInfo = "",
    this.playTime = 1000,
  });

  final String accessKey;
  final String accessKeyId;
  final String gameId;
  final String channelId;
  final String userId;

  @override
  State<HmCloudView> createState() => _HmCloudViewState();
}

class _HmCloudViewState extends State<HmCloudView> {
  @override
  Widget build(BuildContext context) {
    return Container(
      child: _buildView(),
    );
  }

  Widget _buildView() {
    var createParams = <String, dynamic>{
      ///给原生传递初始化参数 就是上面定义的初始化参数
      'accessKey': widget.accessKey,
      'accessKeyId': widget.accessKeyId,
      'gameId': widget.gameId,
      'channelId': widget.channelId,
      'userId': widget.userId,
      'isPortrait': widget.isPortrait,
      'extraInfo': widget.extraInfo,
      'playTime': widget.playTime,
    };

    if (Platform.isIOS) {
      return UiKitView(
        viewType: 'hmCloudView',
        creationParams: createParams,

        /// 用来编码 creationParams 的形式，可选 [StandardMessageCodec], [JSONMessageCodec], [StringCodec], or [BinaryCodec]
        /// 如果存在 creationParams，则该值不能为null
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (Platform.isAndroid) {
      return _getAndroidView(createParams);
    } else {
      return const Text('因为这里只介绍。iOS 、Android，其平台不支持');
    }
  }

  Widget _getAndroidView(Map<String, dynamic> creationParams) {
    const String viewType = 'plugins.flutter.io/hm_cloud_view';
    return PlatformViewLink(
      surfaceFactory:
          (BuildContext context, PlatformViewController controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (PlatformViewCreationParams params) {
        return PlatformViewsService.initExpensiveAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
      viewType: viewType,
    );
  }
}
