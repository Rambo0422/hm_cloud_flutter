class HMCloudConstants {

  HMCloudConstants._();

  /// 通道名称
  static const methodChannelName = "hm_cloud_controller";

  /// viewType
  static const iosViewType = "hmCloudView";
  static const androidViewType = "plugins.flutter.io/hm_cloud_view";

  /// 开始云游戏
  static const startCloudGame = "startCloudGame";

  /// 结束云游戏
  static const stopGame = "stopGame";

  /// 发送按键事件
  static const sendCustomKey = "sendCustomKey";

  /// 错误信息
  static const showInput = "showInput";

  /// 鼠标灵敏度
  static const setMouseSensitivity = "setMouseSensitivity";

  /// 鼠标模式
  static const setMouseMode = "setMouseMode";

  /// callback action
  /// 原生回调定义

  // 延迟信息
  static const delayInfo = 'delayInfo';
  // 游戏停止
  static const gameStop = 'gameStop';
  // 错误信息
  static const errorInfo = "errorInfo";
  // 首帧到达
  static const firstFrameArrival = "firstFrameArrival";
  // 错误信息
  static const queueInfo = "queueInfo";

  // 查询当前房间内⽤户
  static const controlQuery = "controlQuery";
}
