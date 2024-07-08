import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:hm_cloud/hm_cloud_constants.dart';

class HmCloudView extends StatelessWidget {
  const HmCloudView({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.black,
      child: _buildView(),
    );
  }

  Widget _buildView() {
    if (Platform.isIOS) {
      return const UiKitView(viewType: HMCloudConstants.iosViewType);
    } else if (Platform.isAndroid) {
      return _getAndroidView();
    } else {
      return const Text('这里目前只支持iOS 、Android，其平台不支持', style: TextStyle(color: Colors.white));
    }
  }

  Widget _getAndroidView() {
    return PlatformViewLink(
      viewType: HMCloudConstants.androidViewType,
      surfaceFactory: (BuildContext context, PlatformViewController controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (PlatformViewCreationParams params) {
        return PlatformViewsService.initExpensiveAndroidView(
          id: params.id,
          viewType: HMCloudConstants.androidViewType,
          layoutDirection: TextDirection.ltr,
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () => params.onFocusChanged(true),
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}
