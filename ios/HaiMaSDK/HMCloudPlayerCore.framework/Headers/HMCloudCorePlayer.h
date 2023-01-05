//
//  HMCloudCorePlayer.h
//  HMCloudCore
//
//  Created by Apple on 2018/5/12.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "HMCloudCoreMarco.h"
#import "HMCCPayloadData.h"
#import "HMCloudCorePlayerViewController.h"

typedef NS_ENUM(NSInteger,HMLanguageType){
    HMLanguageTypeZh_CN = 0,         //中文
    HMLanguageTypeEn_US,             //英语
};

typedef NS_ENUM(NSInteger,CloudPlayerTimeoutStatus) {
    CloudPlayerGetStreamTimeout             = 100999001,          //没流地址211、201 未返回成功
    CloudPlayerSaasConnectTimeout           = 100999002,          //没流地址211、201成功 Access长连接失败
    CloudPlayerPingpongTimeout              = 100999003,          //没流地址211、201成功 Access长连接成功 乒乓状态异常
    CloudPlayerNoStreamTimeout              = 100999004,          //没流地址211、201成功 Access长连接成功 乒乓状态正常
    CloudPlayerFirstFrameArrivalTimeout     = 100999005,          //有流地址video成功，没收到第一帧
    CloudPlayerOnlyVideoTimeout             = 100999006,          //有流地址video失败，audio成功
    CloudPlayerVideoAndAudioTimeout         = 100999007,          //有流地址video失败，audio失败，input成功
    CloudPlayerVideoAndAudioAndInputTimeout = 100999008,          //有流地址video失败，audio失败，input失败
    CloudPlayerStreamErr                    = 100999009,          //流类型错误，saas返回流类型与sdk流类型不匹配
};

typedef NS_ENUM(NSInteger, CloudCoreStreamingType) {
    CloudCoreStreamingTypeRTMP           = 0,
    CloudCoreStreamingTypeRTC            = 1,
};

typedef NS_ENUM(NSInteger, CloudCorePlayerNetStatus) {
    NetStatusUnknown          = -1,
    NetStatusNotReachable     = 0,
    NetStatusReachableViaWWAN = 1,
    NetStatusReachableViaWiFi = 2,
};

typedef NS_ENUM(NSInteger, CloudCorePlayerStatus) {
    PlayerStatusNone            = 0x0,
    PlayerStatusPreparing       = 0x1,
    PlayerStatusPrepared        = PlayerStatusPreparing<<1,
    PlayerStatusStarted         = PlayerStatusPrepared<<1,
    PlayerStatusPlaying         = PlayerStatusStarted<<1,
    PlayerStatusPaused          = PlayerStatusPlaying<<1,
    PlayerStatusStopped         = PlayerStatusPaused<<1,
    PlayerStatusCanResume       = (PlayerStatusStarted|PlayerStatusPlaying),
};

// 设置x86鼠标类型
typedef NS_ENUM(NSInteger, HMCloudCoreTouchMode) {
    HMCloudCoreTouchModeNone = 0, // 关 不传递数据
    HMCloudCoreTouchModeMouse = 1, // 滑鼠模式
    HMCloudCoreTouchModeScreen = 2, // 多点触控模式
    HMCloudCoreTouchModeFingerTouch = 3, // 手指触控模式
};

typedef NS_ENUM(NSInteger,HMCloudPlayerOperationType){
    HMCloudPlayerOperationTypeNone,
    HMCloudPlayerOperationTypeXbox,
    HMCloudPlayerOperationTypeKeyboard,
};

typedef void (^HMCloudFileImageListBlock)(BOOL result, NSArray *imageList,NSString *errorMsg);

@interface HMCloudCorePlayer : NSObject

const extern NSString *CloudGameOptionKeyId;                    //gameId: 游戏ID
const extern NSString *CloudGameOptionKeyOrientation;           //gameOrientation:游戏横竖屏 0-横屏 1-竖屏
const extern NSString *CloudGameOptionKeyIsRotating;            //isRotating:是否需要支持游戏中屏幕旋转，默认不支持  0:否 1:是
const extern NSString *CloudGameOptionKeyUserId;                //userId:用户ID
const extern NSString *CloudGameOptionKeyUserToken;             //userToken:用户Token
const extern NSString *CloudGameOptionKeyCToken;                //cToken:
const extern NSString *CloudGameOptionKeyExtraId;               //extraId:
const extern NSString *CloudGameOptionKeyShowSize;              //showSize:展示view的尺寸
const extern NSString *CloudGameOptionKeyShowPoint;             //showPoint:展示view的位置
const extern NSString *CloudGameOptionKeyResetPlayerFrame;      //resetPlayerFrame:根据设备方向重置PlayerFrame 0:否 1:是
const extern NSString *CloudGameOptionKeyCameraPermissionCheck; //camera:是否需要每次授权相机使用（*此项功能需要海马侧同时设置才能生效） 0:否 1:是

//播流统计参数相关
const extern NSString *CloudGameOptionKeyStasticFPSInterval;    //帧数统计时长，单位：秒
const extern NSString *CloudGameOptionKeyStasticBandInterval;   //流量统计时长，单位：秒
const extern NSString *CloudGameOptionKeyStasticMaxFramesCount; //流量统计周期内，最大的帧数
const extern NSString *CloudGameOptionKeyStasticDecodeInterval; //平均解码耗时统计时长，单位：秒

const extern NSString *CloudGameOptionKeyEnableVideoFrameRenderCallback;
const extern NSString *CloudGameOptionKeyEnableIpChangedCallback; //ip变化回调

+ (instancetype) sharedCloudPlayer;

@property (nonatomic, copy)             NSString *accessKeyId;      //初始化的accessKeyId
@property (nonatomic, copy)             NSString *channelId;        //渠道号
@property (nonatomic, strong)           NSDictionary *launchOptions;

@property (nonatomic, strong)           NSNumber *deviceId;         //全局唯一的did
@property (nonatomic, copy)             NSString *eventTransId;     //客户端事件流水号-上报使用
@property (nonatomic, copy)             NSString *clientTransId;    //客户端事件流水ID
@property (nonatomic, assign)           NSInteger clientTransSeq;   //客户端事件序号
@property (nonatomic, copy)             NSString *serverTimestamp;  //客户端事件记录的服务器时间戳
@property (nonatomic, copy)             NSString *countlyUrl;
@property (nonatomic, copy)             NSString *countlyAppkey;

@property (nonatomic, copy)             NSString *packageName;      //游戏包名
@property (nonatomic, copy)             NSString *userId;
@property (nonatomic, copy)             NSString *userToken;
@property (nonatomic, copy)             NSString *cToken;
@property (nonatomic, copy)             NSString *extraId;
@property (nonatomic, assign)           HMCloudCorePlayerOrientation orientation;
@property (nonatomic, assign)           BOOL isRotating;


@property (nonatomic, copy)             NSString *cloudId;
@property (nonatomic, copy)             NSString *sign;

@property (nonatomic, assign)           int64_t stasticReportPostFailedCount;

@property (nonatomic, assign)           CloudCorePlayerStatus playerStatus;


@property (nonatomic, copy)             NSString *sdkVersion;       //sdk版本号
@property (nonatomic, assign)           CGFloat autoModifyBrightness;   //server配置亮度
@property (nonatomic, copy)             NSString  *appChannel;
@property (nonatomic, assign)           CloudCoreStreamingType cloudStreamingType;
@property (nonatomic, assign)           BOOL isGetControlUser;
@property (nonatomic, assign)           CGSize     showViewSize;
@property (nonatomic, assign)           CGPoint    showViewPoint;
@property (nonatomic, assign)           BOOL resetPlayerFrame;
@property (nonatomic, assign)           BOOL cameraPermissionCheck;
@property (nonatomic, assign)           BOOL isGetStreamUrlSuccess;
@property (nonatomic, assign)           BOOL isGetCloudServiceSuccess;
@property (nonatomic, assign)           BOOL isNotMatchStreamType;
@property (nonatomic, copy)             NSString *reportStatusCode;
@property (nonatomic, assign)           BOOL enableNotifiyIpChangedCallback;

- (NSString *) getFinalCountlyUrl;
- (NSString *) getFinalCountlyKey;

/**
 向海马云端注册

 @param accessKeyID 接入商ID
 @param channelId 接入商渠道ID
 @param launchOptions AppLauchuOptions
 @return 是否正常进行注册，最终注册结果异步返回
 */
- (BOOL) registCloudPlayer:(NSString *)accessKeyID
                 channelId:(NSString *)channelId
                   options:(NSDictionary *)launchOptions;

/**
 向海马云端注册

 @param accessKeyID 接入商ID
 @param channelId 接入商渠道ID
 @param language 语言类型
 @param launchOptions AppLauchuOptions
 @return 是否正常进行注册，最终注册结果异步返回
 */
- (BOOL) registCloudPlayer:(NSString *)accessKeyID
                 channelId:(NSString *)channelId
                 language:(HMLanguageType)language
                   options:(NSDictionary *)launchOptions;

/**
 准备游戏

 @param options 游戏参数
 @return 云游戏ViewController
 */
- (UIViewController *) prepare:(NSDictionary *)options;

- (BOOL) checkPlayerOptions;

- (void) playWithVideoUrl:(NSString *)videoUrl
                 audioUrl:(NSString *)audioUrl
                 inputUrl:(NSString *)inputUrl
                screenUrl:(NSString *)screenUrl
         switchBRDuration:(int)switchBRDuration
           detectInterval:(int)detectInterval
  delayTimeSinceLastCheck:(int)delayTimeSinceLastCheck
         switBRFrozenTime:(int)switBRFrozenTime
       isReportDetailInfo:(int)isReportDetailInfo
      delaySampleInterval:(int)delaySampleInterval
           startDelayTime:(int)startDelayTime
   websocketRetryMaxTimes:(int)websocketRetryMaxTimes
       sdkStokenValidTime:(int)sdkStokenValidTime
                   hidden:(BOOL)hidden
        notForceReconnect:(BOOL)notForceReconnect;

- (void) setPingPongParams:(NSInteger)interval
                 delayTime:(NSInteger)delayTime
            traceRouteTime:(NSInteger)traceRouteTime;

- (void) setPing2PongParams:(NSString *)interval ReportDelayTimes:(NSInteger)reportDelayTimes;

- (void) startStastics;

/**
 @param webrtcConnectTime 超时时间
 @param webrtcConnectRtmpSwitch webrtc连接失败切rtmp开关。默认开启
 @param webrtcConnectRetryCount webrtc连接失败重连次数。默认是5次，设置为0同时打开开关就跳过重连切rtmp
 @param soketTimeout socket连接signal server超时时间
 @param soketReconnectDelayMax socket重接signal server延时
 @param soketReconnectAttempts socket重连signal server次数
 @param dropClientCandidate 灰度值 0 client所有外网candidate全部发往Streamer 100: client所有外网candidate只在本地起作用，不发给Streamer
 @param connPingIntervalMs ping间隔，单位：ms
 @param unWritableTimeoutMs 超时断开时间，单位：ms
 @param unWritableMinChecks 超时前最大ping次数
 @param websocketRetryMaxTimes WebSocket在sToken有效期内最大重连次数
 @param sdkStokenValidTime 对SDK的sToken有效期，单位秒
 @param fpsTimeout fps一直为0时超时时间
 @param openCameraPermissionCheck app设置打开摄像头许可检查
 @param openCameraPermissionCheckByServer server配置打开摄像头许可检查
 @param config mongo中的rtc配置参数
 */
 - (void)setWebrtcConnectTime:(float)webrtcConnectTime
      webrtcConnectRtmpSwitch:(BOOL)webrtcConnectRtmpSwitch
      webrtcConnectRetryCount:(int)webrtcConnectRetryCount
                 soketTimeout:(float)soketTimeout
       soketReconnectDelayMax:(float)soketReconnectDelayMax
       soketReconnectAttempts:(NSInteger)soketReconnectAttempts
          dropClientCandidate:(int)dropClientCandidate
           connPingIntervalMs:(int)connPingIntervalMs
          unWritableTimeoutMs:(int)unWritableTimeoutMs
          unWritableMinChecks:(int)unWritableMinChecks
       websocketRetryMaxTimes:(int)websocketRetryMaxTimes
           sdkStokenValidTime:(int)sdkStokenValidTime
                   fpsTimeout:(float)fpsTimeout
    openCameraPermissionCheck:(BOOL)openCameraPermissionCheck
 openCameraPermissionCheckByServer:(BOOL)openCameraPermissionCheckByServer
                       config:(NSDictionary *)config;

/**
 刷新SToken后重连inputUrl、screenUrl
 @param inputUrl  操作连接地址，ws协议
 @param screenUrl 小屏连麦长链接
 */
- (void)playerRetryInputUrl:(NSString *)inputUrl
                  screenUrl:(NSString *)screenUrl;


/**
 暂停游戏
 */
- (void) pause;
- (BOOL) resume;

/**
 停止游戏
 */
- (void) stop;
- (void) stop:(int)seconds;
/**
 停止游戏，退出游戏界面

 @param animated 和presentViewController 的 animated 值一致
 @param seconds 存档的最小游戏时长,单位：秒
 */
- (void) stopAndDismiss:(BOOL)animated;
- (void) stopAndDismiss:(BOOL)animated archiveMinSeconds:(int)seconds;

- (void) showPlayer;

- (void) connectWebSocket:(NSString *)url;
- (BOOL) reconnectWebSocket;
- (void) disconnectWebSocket;

/**
 获取视频帧延迟

 @return 视频延迟，单位ms
 */
- (NSInteger) getVideoLatency;

/**
 设置背景图
 */
- (void) setBackgroundImage:(UIImage *)bgImage;

/**
 发送消息

 @param message 消息内容
 */
- (BOOL) sendMessage:(NSString *)message;
- (BOOL) sendKeycode:(NSInteger)keycode;
- (BOOL) sendCommand:(NSString *)cmd updateUserOperationTime:(BOOL)updateUserOperationTime;
- (BOOL) sendScreenWSMessage:(NSString *)msg;
- (BOOL) sendText:(NSString *)text;

- (void) addSubView:(UIView *)view;

- (void) centerSubView:(UIView *)view style:(HMCloudCorePlayerSubViewCenterStyle)style;

/**
 开始网络状态监控

 @param block 回调通知
 */
- (void) startNetMonitor:(void (^)(CloudCorePlayerNetStatus status))block;

/**
 停止网络状态监控
 */
- (void) stopNetMonitor;

/**
 云游戏，有手机提供静音接口
 @param mute 静音
 */
- (void) setAudioMute:(BOOL)mute;

/**
 设置手机亮度
 */
- (void) setScreenBrightness;

/**
 恢复手机系统亮度
 */
- (void) resetScreenBrightness;

/**
 获取某一秒延迟检测信息
 @return 包含延迟信息的HMDelayInfoModel
 */
- (HMDelayInfoModel *) getDelayInfo;

/**
获取用户最后一次操作时间
@return 用户最后操作时间戳，单位ms
*/
- (long long) getLastUserOperationTimestamp;

/**
启动Webrtc Player
@param model webrtc连接参数模型
@param inputUrl 操作连接地址，ws协议
@param screenUrl 小屏连麦长链接
@param cid 实例id
@param hidden 是否隐藏Player（池化重启，为true；池化非重启，非池化为false）
@param notForceReconnect 不强行重连连接，等待streamer自己重连
 */
- (void) playWithWebrtcModel:(HMCloudPlayerWebRtcInfoModel *)model
                    inputUrl:(NSString *)inputUrl
                   screenUrl:(NSString *)screenUrl
                         cid:(NSString *)cid
                      hidden:(BOOL)hidden
           notForceReconnect:(BOOL)notForceReconnect;

/**
 获取debug延迟信息
 @return 延迟信息
 */
- (NSString *)getDebugDelayInfo;

/**
主动关闭screenUrl连接
*/
- (void)disconnectScreenUrl;

/**
 心跳超时测试
 @param show 是否开启心跳超时
 */
- (void)heartBeatPongTimeoutCore:(BOOL)show;

/**
 刷新stoken,取消第一帧超时，rtc连接超时计时器
 */
- (void)cancelTimerWithRereshStoken;

/**
 获取webrtc版本号
 */
+ (NSString *)getWebRTCVersion;

/**
 当客户端app调用play方法长时间无反应，导致app请求超时，可调用此方法获取sdk运行状态
 */
- (CloudPlayerTimeoutStatus) getCloudPlayerTimeoutStatus;

/*
 RTMP推流参数设置
 @param probesize 数据量大小
 @param analyzeduration 时长
 @param fpsprobesize 帧大小
 @param framedropaudio 音频帧丢帧开关
 @param framedropvideo 视频帧丢帧开关
 */
- (void)setRTMPProbesize:(int)probesize
         analyzeduration:(int)analyzeduration
            fpsprobesize:(int)fpsprobesize
          framedropaudio:(int)framedropaudio
          framedropvideo:(int)framedropvideo;

/**
 设置是否接收帧回调
 @param enabled = YES 接收帧回调 enabled = NO 不接收帧回调
 */
- (void)enableFrameRenderedCallback:(BOOL)enabled;

/**
 x86呼出和隐藏键盘
 @param isOpen = YES呼出，isOpen = NO隐藏
 */
- (void)cloudSwitchKeyboard:(BOOL)isOpen;

/**
 x86设置触屏数据模式
 */
- (void)cloudSetTouchModel:(HMCloudCoreTouchMode)model;

/**
 获取触屏模式
 */
- (HMCloudCoreTouchMode)getMouseType;

/**
启动Webrtc x86 Player
@param model webrtc x86连接参数模型
@param cid 实例id
@param resolutionSize 分辨率
@param gamePadDict 游戏按键参数
@param playWithControl 播放时是否带控制权
@param pkgName 游戏包名
@param userId 用户名
*/
- (void)playWithWebrtcX86Model:(HMCloudPlayerWebRtcInfoModel *)model cid:(NSString *)cid resolutionSize:(NSString *)resolutionSize gamePadDict:(NSDictionary *)gamePadDict playWithControl:(BOOL)playWithControl pkgName:(NSString *)pkgName userId:(NSString *)userId;

/**
 切换手柄模式
 */
- (void)switchOperationType:(HMCloudPlayerOperationType)type;

/**
 获取手柄模式
 */
- (HMCloudPlayerOperationType)getOperationType;

/**
 是否开启x86手柄震动反馈
 @param isOpen BOOL yes 为开启 no为不开启
 */
- (void)setFeedback:(BOOL)isOpen;

/**
 获取反馈震动状态
 */
- (BOOL)getFeedback;

/**
 调节x86手柄透明度
 0-1    0为透明 1为不透明
 */
- (void)setCustomStickAlpha:(float)alpha;

/**
 获取x86手柄透明度
 */
- (float)getCustomStickAlpha;

/**
 设置鼠标灵敏度
 */
- (void)setMouseSensitivity:(float)sensitivity;

/**
 获取鼠标灵敏度
 */
- (float)getMouseSensitivity;

/**
 x86设置显示虚拟按键原始值
 @param isShow = YES 显示，isShow = NO隐藏
 */
- (void)cloudSetShowVirtualRawKey:(BOOL)isShow;

/**
 编辑手柄
 @return yes 可以编辑 no 无法编辑
 */
- (BOOL)editOperationView;

/**
 关闭本地键盘
 */
- (void)hiddenKeyboard;

/**
 获取云游戏图库列表
 @param limit 获取图片列表的数量
 @param offset offset表示分页（如20一页的话，0表示第一页，20表示第二页）
 @param cloudFileImageListBlock result 查询结果，imageList 图片列表 errorMsg result为NO时返回错误原因
 */
- (void)getCloudImageList:(NSInteger)limit offset:(NSInteger)offset cloudFileImageListBlock:(HMCloudFileImageListBlock)cloudFileImageListBlock;

/**
 处理请求图片列表消息
 @param msgDict 图片列表返回信息
 */
- (void)handleImageListResponse:(NSDictionary *)msgDict;

/**
 下载连接失效刷新stoken
 */
- (void)downloadUrlExpireRefreshStoken;

@end

typedef NS_ENUM (NSInteger, CloudPlayerStopReason) {
    CloudPlayerStopReasonInternalError = -1,   //未知内部错误
    CloudPlayerStopReasonNormal,               //正常结束
    CloudPlayerStopReasonTimeout,              //游戏时间到
    CloudPlayerStopReasonNoInputTimeout,       //无操作超时被踢
    CloudPlayerStopReasonInstanceError,        //实例出错
    CloudPlayerStopReasonQueueForbidden,        //排队人数过多，禁止排队
    CloudPlayerStopReasonCrashed,               //实例崩溃
    CloudPlayerStopReasonMaintance,             //维护中
    CloudPlayerStopReasonMultiInstance,         //多开
    CloudPlayerStopReasonTokenExpired,          //token失效
    CloudPlayerStopReasonLowSpeed,              //低于服务下限
    CloudPlayerStopReasonUrlTimeout,            //获取流地址超时
    CloudPlayerStopReasonLoseControl,           //失去控制权
};

@interface HMCloudCorePlayerStopInfo : NSObject

@property (nonatomic, assign) CloudPlayerStopReason reason;
@property (nonatomic, copy)   NSString              *errorCode;
@property (nonatomic, copy)   NSString              *errorMsg;
@property (nonatomic, strong) NSArray               *queues;
@property (nonatomic, copy)   NSString              *openApiReleaseInstance;

+ (instancetype) stopInfo:(CloudPlayerStopReason)reason errorCode:(NSString *)errorCode errorMsg:(NSString *)errorMsg;
+ (instancetype) stopInfo:(CloudPlayerStopReason)reason queues:(NSArray *)queues errorCode:(NSString *)errorCode errorMsg:(NSString *)errorMsg;

@end

@protocol HMCloudCorePlayerProtocol <NSObject>

- (void) saasWebSockectStartConnect:(NSString *)url;
- (void) saasWebSocketDidConnected;
- (void) saasWebSocketDisconnected:(NSError *)error;
- (void) saasWebSocketHeartBeatPongTimeout;

- (void) streamUrlGot:(HMCCPayloadData *)data;

- (void) queueConfirmTip:(HMCCPayloadData *)data;
- (void) queueStatesUpdate:(HMCCPayloadData *)data;
- (void) queueEntering;
- (void) instancePrepared;

- (void) playerTimeout:(HMCCPayloadData *)data;

- (void) messageReceived:(NSString *)msg;
- (void) recordWSMessage:(NSString *)msg;

- (void) maintanceWillStart:(HMCCPayloadData *)data;
- (void) maintanceDone;
- (void) maintanceForbidPlay:(HMCCPayloadData *)data;
- (void) maintanceInProgress:(HMCCPayloadData *)data;

- (void) playingTimeNotification:(HMCCPayloadData *)data;
- (void) playingTimeUpdated:(HMCCPayloadData *)data;
- (void) playingTimeShowToast:(HMCCPayloadData *)data;

- (void) noInputRemindTimeNotification:(HMCCPayloadData *)data;

- (void) cloudPlayerStopped:(HMCloudCorePlayerStopInfo *)info;

- (void) startLivingSuccess;

- (void) startLivingFailed:(NSString *)errorCode;

- (void) stopLivingSuccess;

- (void) stopLivingFailed:(NSString *)errorCode;

- (void) getAuthCodeSuccess:(HMCCPayloadData *)data;

- (void) getAuthCodeFailed:(HMCCPayloadData *)data;

- (void) controlLosed:(HMCCPayloadData *)data;

- (void) getControlSuccess:(HMCCPayloadData *)data;

- (void) getControlFailed:(HMCCPayloadData *)data;

@end


