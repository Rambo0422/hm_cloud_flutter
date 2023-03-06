import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

class HmCloudView extends StatefulWidget {
  const HmCloudView({super.key});

  // View 是否横屏或者竖屏
  // final bool isPortrait;
  // final String extraInfo;
  // final int playTime;
  // final int videoViewType;

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
    if (Platform.isIOS) {
      return const UiKitView(
        viewType: 'hmCloudView',
      );
    } else if (Platform.isAndroid) {
      return _getAndroidView();
    } else {
      return const Text('因为这里只介绍。iOS 、Android，其平台不支持');
    }
  }

  Widget _getAndroidView() {
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
