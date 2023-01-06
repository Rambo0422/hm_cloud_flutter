import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HmCloudView extends StatefulWidget {
  const HmCloudView(
      {super.key,
      required this.accessKey,
      required this.accessKeyId,
      required this.gameId,
      required this.channelId,
      required this.userId});

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
    if (Platform.isIOS) {
      return UiKitView(
        viewType: 'hmCloudView',
        creationParams: <String, dynamic>{
          ///给原生传递初始化参数 就是上面定义的初始化参数
          'accessKey': widget.accessKey,
          'accessKeyId': widget.accessKeyId,
          'gameId': widget.gameId,
          'channelId': widget.channelId,
          'userId': widget.userId,
        },

        /// 用来编码 creationParams 的形式，可选 [StandardMessageCodec], [JSONMessageCodec], [StringCodec], or [BinaryCodec]
        /// 如果存在 creationParams，则该值不能为null
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (Platform.isAndroid) {
      return const AndroidView(viewType: 'testView');
    } else {
      return const Text('因为这里只介绍。iOS 、Android，其平台不支持');
    }
  }
}
