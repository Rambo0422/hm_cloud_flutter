//
//  HMCloudCorePlayerViewController.h
//  HMCloudCore
//
//  Created by Apple on 2018/5/10.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HMCloudCoreMarco.h"
#import "HMCloudPlayerWebRtcInfoModel.h"
#import "HMInputOpData.h"
#import "HMStreamingFileModel.h"

@class HMDelayInfoModel;

typedef NS_ENUM(NSInteger, HMCloudCorePlayerEvent) {
    HMCloudCorePlayerEventAudioConnectionStarted,
    HMCloudCorePlayerEventAudioConnectionSuccessed,
    HMCloudCorePlayerEventAudioConnectionFailed,

    HMCloudCorePlayerEventVideoConnectionStarted,
    HMCloudCorePlayerEventVideoConnectionSuccessed,
    HMCloudCorePlayerEventVideoConnectionFailed,

    HMCloudCorePlayerEventInputConnectionStarted,
    HMCloudCorePlayerEventInputConnectionSuccessed,
    HMCloudCorePlayerEventInputConnectionFailed,
    HMCloudCorePlayerEventInputConnectionClosed,

    HMCloudCorePlayerEventScreenURLInvalid,
    HMCloudCorePlayerEventScreenConnectionStarted,
    HMCloudCorePlayerEventScreenConnectionSuccessed,
    HMCloudCorePlayerEventScreenConnectionClosed,
    HMCloudCorePlayerEventScreenConnectionAgain,

    HMCloudCorePlayerEventPacketDelayInfoReport,
    HMCloudCorePlayerEventPacketDecodeTime,
    HMCloudCorePlayerEventPingPongTime,
    HMCloudCorePlayerEventPingPongDelay,
    HMCloudCorePlayerEventPingPongTraceRoute,
//    HMHMCloudCorePlayerEventTimeCalibrateDelay,
    HMCloudCorePlayerEventPhoneix2Delay,

    HMCloudCorePlayerEventSignalConnecting,             //WEBRTC开始连接/开始连接signalServer
    HMCloudCorePlayerEventPeerConnected,                //WEBRTC连接成功 pc成功
    HMCloudCorePlayerEventPeerConnectionFailed,         //PeerConnectionFailed
    HMCloudCorePlayerEventSignalFailed,                 //WEBRTC信令服务链接超时 socketIO默认超时
    HMCloudCorePlayerEventSignalSendJoin,               //WEBRTC发送join
    HMCloudCorePlayerEventSignalRecvOffer,              //WEBRTC收到offer
    HMCloudCorePlayerEventPeerCreateAnswer,             //WEBRTC创建answer
    HMCloudCorePlayerEventSignalSendAnswer,             //WEBRTC发送answer
    HMCloudCorePlayerEventSignalRecvCandidate,          //WEBRTC收到candidate
    HMCloudCorePlayerEventSignalSendCandidate,          //WEBRTC发送candidate
    HMCloudCorePlayerEventPeerDisconnected,             //WEBRTC的client端PeerconnectionState=disconnected
    HMCloudCorePlayerEventIceDisconnected,              //WEBRTC的client端Peer iceConnectionState=disconnected
    HMCloudCorePlayerEventReconnect,                    //WEBRTC重连
    HMCloudCorePlayerEventClosed,                       //WEBRTC关闭close
    HHMCloudCorePlayerEventPeerSetRemoteSDPFailed,      //WEBRTC执行接口setRemoteDescription失败
    HMCloudCorePlayerEventPeerCreateAnswerFailed,       //WEBRTC创建answer失败
    HMCloudCorePlayerEventPeerSetLocalSDPSuccessed,     //WEBRTC执行setLocalDescription成功
    HMCloudCorePlayerEventPeerSetLocalSDPFailed,        //WEBRTC执行setLocalDescription失败
    HMCloudCorePlayerEventPeerOnTrack,                  //WEBRTC执行ontrack，获取流内容
    HMCloudCorePlayerEventSignalConnected,              //WEBRTC信令服务连接成功
    HMCloudCorePlayerEventPeerAddIceCandidate,          //WEBRTC addIceCandidate 成功
    HMCloudCorePlayerEventSignalDisconnect,             //WEBRTC addIceCandidate 成功
    HMCloudCorePlayerEventCodecType,                    //解码格式
    HMCloudCorePlayerEventAnalyseLog,                   //分析问题log
    HMCloudCorePlayerEventUncertain,                    //rtc相关log不确定eventId
    HMCloudCorePlayerEventRereceive,                    //13248 WEBRTC收到第一帧
    HMCloudCorePlayerEventDecoded,                      //13249 WEBRTC解码完第一帧
    HMCloudCorePlayerEventWebrtcConnectionFailed,        //WEBRTC连接失败
    HMCloudCorePlayerEventDelayInfoFailed,              //延迟上报未达到条件无法上报16003
    HMCloudCorePlayerEventCreateOfferFailed,           //13261 WEBRTC创建offer失败
    HMCloudCorePlayerEventStartRequestSDP,             //13262 WEBRTC开始请求sdp
    HMCloudCorePlayerEventRequestSDPSuccess,           //13263 WEBRTC请求sdp成功
    HMCloudCorePlayerEventRequestSDPFail,              //13264 WEBRTC请求sdp失败
    HMCloudCorePlayerEventSetRemoteDescriptionFail,    //13265 WEBRTCsetRemoteDescription失败
    HMCloudCorePlayerEventDataChannelState,            //13266 WEBRTCDataChannelStateChanged
    HMCloudCorePlayerEventGSMNotificationGameOver,     //13267 GSMNotification结束游戏
    HMCloudCorePlayerEventGSMNotificationLoseControl,  //13315 GSMNotification 失去控制权
    HMCloudCorePlayerEventGSMNotificationNoInputTimeout,//GSMNotification 长时间无输入退出
    HMCloudCorePlayerEventGSMNotificationGetControl,    //13313 GSMNotification 获得控制权
    HMCloudCorePlayerEventGSMNotificationKeyboardChanged, //12701 键盘状态变化通知
    HMCloudCorePlayerEventStartAskAnswer,                //13268 WEBRTC 开始askAnswer
    HMCloudCorePlayerEventAskAnswerSuccess,              //13269 WEBRTC askAnswer成功
    HMCloudCorePlayerEventAskAnswerFail,                 //13270 WEBRTC askAnswer失败
    HMCloudCorePlayerEventGetScreenshotStart,            //12880 开始获取游戏截屏
    HMCloudCorePlayerEventGetScreenshotSuccess,          //12881 获取游戏截屏成功
    HMCloudCorePlayerEventGetScreenshotTimeOut,          //12882 获取游戏截屏超时
    HMCloudCorePlayerEventGetScreenshotFail,             //12883 获取游戏截屏失败

    HMCloudCorePlayerEventDataChannelCreateDevice,      //WEBRTC DataChannel创建device
    HMCloudCorePlayerEventDataChannelReceiveFirstBinary,//WEBRTC DataChannel当收第一个二进制指令时上报，且仅上报一次
    HMCloudCorePlayerEventDataChannelReceiveFirstText,  //WEBRTC DataChannel当收第一个文本指令时上报，且仅上报一次
    HMCloudCorePlayerEventDataChannelSendFirstBinary,   //WEBRTC DataChannel发送第一个二进制数据包时上报
    HMCloudCorePlayerEventDataChannelSendFirstText,     //WEBRTC DataChannel发送第一个文本指令时上报，且仅上报一次
    HMCloudCorePlayerEventDataChannelCloseDevice,       //WEBRTC DataChannel device关闭
    HMCloudCorePlayerEventCameraNotPermission,          //相机权限未授予
    HMCloudCorePlayerEventCameraApplyPermission,        //正在申请相机权限
    HMCloudCorePlayerEventCameraPermissionCheck,        //相机权限检查

    HMCloudCorePlayerEventSwitchIMECalled,  //切换键盘被调用
    HMCloudCorePlayerEventSwitchIMEResult,  //切换键盘结果

    HMCloudCorePlayerEventGetIMEStart,   //开始获取键盘类型
    HMCloudCorePlayerEventGetIMEResult,  //获取键盘类型结果
};

typedef NS_ENUM(NSInteger, HMCloudCorePlayerSubViewCenterStyle) {
    SubViewCenterStyleHorizitional  =                               0x01,
    SubViewCenterStyleVertical      = SubViewCenterStyleHorizitional<<1,
    SubViewCenterStyleBoth          = (SubViewCenterStyleHorizitional|SubViewCenterStyleVertical),
};

typedef NS_ENUM(NSInteger, HMCloudCorePlayerFileListStatus) {
    HMCloudCorePlayerFileListStatusSuccess,      //列表获取成功
    HMCloudCorePlayerFileListStatusDisconnect,   //链接断开
    HMCloudCorePlayerFileListStatusTimeout,      //请求超时
    HMCloudCorePlayerFileListStatusNotInit,      //未初始化
};

typedef void (^HMCloudCorePlayerFileListBlock)(BOOL result, NSArray *fileList, HMCloudCorePlayerFileListStatus status);

typedef NS_ENUM(NSInteger,HMCloudCorePlayerMouseType){
    HMCloudCorePlayerMouseTypeDisable = 0,
    HMCloudCorePlayerMouseTypeRelativeMove,
    HMCloudCorePlayerMouseTypeAbsoluteMove,
    HMCloudCorePlayerMouseTypeSwipeMove,
    HMCloudCorePlayerMouseTypeCustomRelativeMove,
    HMCloudCorePlayerMouseTypeFingerTouch,
};

typedef NS_ENUM(NSInteger,HMCloudCorePlayerOperationType){
    HMCloudCorePlayerOperationTypeNone,
    HMCloudCorePlayerOperationTypeXbox,
    HMCloudCorePlayerOperationTypeKeyboard,
};

typedef NS_ENUM(NSInteger,HMCloudCorePlayerDataChannelType) {
    HMCloudCorePlayerDataChannelTypePingPong,
    HMCloudCorePlayerDataChannelTypePay,
    HMCloudCorePlayerDataChannelTypeDataClipboard,
    HMCloudCorePlayerDataChannelTypeDataShellCommonds,
};

typedef NS_ENUM(NSInteger,HMCloudCorePlayerRecordStatusType) {
    HMCloudCorePlayerRecordStatusTypeStartRecordSuccess = 0,  // 开启录音成功
    HMCloudCorePlayerRecordStatusTypeStartRecord,             // 开始录音
    HMCloudCorePlayerRecordStatusTypeStopRecord,              // 停止录音
    HMCloudCorePlayerRecordStatusTypeUnknown,                 // 异常
};

typedef NS_ENUM(NSInteger, HMCloudCorePlayerWebsocketType) {
    HMCloudCorePlayerWebsocketTypeInput,
    HMCloudCorePlayerWebsocketTypeScreen,
};

typedef NS_ENUM(NSInteger,CloudCorePlayerScreenshotStatus){
    CloudCorePlayerScreenshotStatusSuccess,
    CloudCorePlayerScreenshotStatusTimeout,
    CloudCorePlayerScreenshotStatusInternalError,
};

typedef NS_ENUM(NSInteger, CloudCorePlayerUploadResponseStatus) {
    CloudCorePlayerUploadResponseStatusSuccess,        //上传成功
    CloudCorePlayerUploadResponseStatusEmpty,          //上传队列空
    CloudCorePlayerUploadResponseStatusTimeout,        //超时
    CloudCorePlayerUploadResponseStatusCancel,         //取消
    CloudCorePlayerUploadResponseStatusIncorrectFormat,//格式不正确
    CloudCorePlayerUploadResponseStatusBeyondMaxLimit, //超过最大文件限制
    CloudCorePlayerUploadResponseStatusInternalError,  //内部错误
    CloudCorePlayerUploadResponseStatusDisconnect,     //链接断开
};

typedef NS_ENUM (NSInteger, CloudCorePlayerFileAction) {
    CloudCorePlayerFileActionDownloadStart = 0,             //12611 开始下载
    CloudCorePlayerFileActionDownloadSuccess,               //12612 文件下载成功
    CloudCorePlayerFileActionDownloadStop,                  //12613 文件下载停止
    CloudCorePlayerFileActionDownloadCancel,                //12614 文件取消下载
    CloudCorePlayerFileActionnDownloadError,                //12615 下载错误
    CloudCorePlayerFileActionDownloadComplete,              //12616 下载完成
    CloudCorePlayerFileActionDownloadProgress,              //12617 下载状态
    CloudCorePlayerFileActionUploadStart,                   //12601 开始上传
    CloudCorePlayerFileActionUploadSuccess,                 //12602 文件上传成功
    CloudCorePlayerFileActionUploadStop,                    //12603 文件上传停止
    CloudCorePlayerFileActionUploadCancel,                  //12604 文件取消上传
    CloudCorePlayerFileActionUploadError,                   //12605 上传错误
    CloudCorePlayerFileActionUploadComplete,                //12606 上传完成
    CloudCorePlayerFileActionUploadProgress,                //12607 上传状态
};

typedef NS_ENUM(NSInteger, CloudCorePlayerFileDownloaderResponseStatus) {
    CloudCorePlayerFileDownloaderResponseStatusSuccess,             //下载成功
    CloudCorePlayerFileDownloaderResponseStatusCancelList,          //下载列表中取消下载
    CloudCorePlayerFileDownloaderResponseStatusCancelSuccess,       //长连接取消下载成功
    CloudCorePlayerFileDownloaderResponseStatusLimited,             //下载受限
    CloudCorePlayerFileDownloaderResponseStatusTimeout,             //下载超时
    CloudCorePlayerFileDownloaderResponseStatusSaveFailed,          //保存失败
    CloudCorePlayerFileDownloaderResponseStatusKeepAliveTimeout,    //保活时间用尽
};

typedef NS_ENUM(NSInteger, CloudCorePlayerCancelDownloadResponseStatus) {
    CloudCorePlayerCancelDownloadResponseStatusSuccess,      //取消成功
    CloudCorePlayerCancelDownloadResponseStatusEmpty,        //下载队列空
    CloudCorePlayerCancelDownloadResponseStatusOutList,      //不在下载列表
    CloudCorePlayerCancelDownloadResponseStatusDownloaded,   //已下载完成
    CloudCorePlayerCancelDownloadResponseStatusTimeout,      //取消超时
    CloudCorePlayerCancelDownloadResponseStatusDisconnect,   //链接断开
};

typedef NS_ENUM(NSInteger, CloudCorePlayerWSMessageType) {
    CloudCorePlayerWSMessageTypeNormal,
    CloudCorePlayerWSMessageTypeClipboard,
    CloudCorePlayerWSMessageShellCommands,
};

typedef NS_ENUM (NSInteger,CloudCorePlayerIMEType){
    CloudCorePlayerIMETypeCloud,       //云端键盘
    CloudCorePlayerIMETypeLocal,       //本地键盘
};

typedef NS_ENUM (NSInteger,CloudCorePlayerIMEResponseStatus) {
    CloudCorePlayerIMEResponseStatusSuccess,//切换成功
    CloudCorePlayerIMEResponseStatusUnsupported,//不支持切换
    CloudCorePlayerIMEResponseStatusTimeout,//切换超时
    CloudCorePlayerIMEResponseStatusRomReject,//切换失败
};

typedef void (^CloudCorePlayerScreenshotBlock)(BOOL result,NSData *data,CloudCorePlayerScreenshotStatus status,NSString *errorMsg);

typedef void (^CloudCorePlayerUploadResponseBlock)(BOOL result, CloudCorePlayerUploadResponseStatus status ,NSString *errorMsg, HMStreamingFileModel *file);

typedef void (^CloudCorePlayerUploadComplete)(void);

typedef void (^CloudCorePlayerDownloadResponseBlock)(BOOL result, CloudCorePlayerFileDownloaderResponseStatus status ,NSString *errorMsg, HMStreamingFileModel *file);

typedef void (^CloudCorePlayerDownloadComplete)(void);

typedef void (^CloudCorePlayerDownloadProgressBlock)(double downloadProgress,HMStreamingFileModel *file);

typedef void (^CloudCoreFileCancelDownloadResponseBlock)(BOOL result, CloudCorePlayerCancelDownloadResponseStatus status,HMStreamingFileModel *file);

typedef void (^CloudCoreFileCancelDownloadComplete)(void);

typedef void (^CloudCorePlayerIMECompletion)(CloudCorePlayerIMEResponseStatus status);

@protocol HMCloudCorePlayerViewControllerDelegate <NSObject>
@optional

/**
 重置stoken次数
 */
- (void) resetRefreshStokenTimes;

/**
 超出重连最大次数
 */
- (void) beyondReconnectPlayerTimes;

/**
 播放器出错
 @param isPlay  告知是否需要继续播流
 */
- (void) cloudCorePlayerErrorIsPlay:(BOOL)isPlay;

/**
收到视频第一帧
 */
- (void) cloudCorePlayerFirstVideoFrameArrival;

/// 视频渲染一帧
/// @param timestamp  视频帧时间戳，单位：ms
- (void) cloudCorePlayerDidRenderVideoFrameAt:(int64_t)timestamp;

/**
 检测到帧延迟，需要切换清晰度

 @param percent 帧延迟百分比
 */
- (void) cloudCorePlayerPacketDelayOccured:(NSString *)percent;

/**
 收到统计信息

 @param type 信息类型 0-bandwidth 1-frames 2-decodetime
 @param value 对应的统计值
 @param detail 最大Bytes的帧列表，仅对type==0有效
 */
- (void) cloudCorePlayerStasticInfoArrival:(int)type value:(int64_t)value detail:(NSArray *)detail;

/**
 系统音量发生变化

 @param up true:音量+ false:音量-
 */
- (void) systemVolumeChanged:(BOOL)up;

/**
 点击事件触发
 */
- (void) cloudCorePlayerTouchBegan;

/**
 点击事件触发
 
 @param touches 触摸事件的集合
 */
- (void) cloudCorePlayerTouchBegan:(NSSet<UITouch *> *)touches;

/**
 有事件需要上报

 @param event 事件类型
 @param data 扩展数据
 @param eventId 事件id
 */

- (void) cloudCorePlayerEventReport:(HMCloudCorePlayerEvent)event data:(NSString *)data eventId:(NSInteger)eventId;

/**
 触发权限使用弹窗

 @param type 权限类型Microphone麦克风；
 @param success 授权回调；
 */
- (void) cloudCorePlayerUsageAuthorization:(HMCloudPlayerUsageAuthorization)type success:(void (^)(BOOL authorization))success;

/// 收到WS消息
/// @param msg 消息字典
- (void) cloudCorePlayerDidReceiveWSMessage:(NSDictionary *)msg;

/// 本地键盘状态通知
/// @param hidden 是否隐藏
- (void) cloudCorePlayerKeyboardStatus:(BOOL)hidden;

/**
 @param timerId  对象hash值
 @param timerKey timer类型
 @param duration timer时长
 @param isRepeat timer是否重复
 @param action   timerAction
 */
- (void)reportTimerWithFormat:(NSInteger)timerId timerKey:(NSString *)timerKey duration:(NSInteger) duration isRepeat:(BOOL)isRepeat action:(NSString *)action;

/**
 WebRtc切Rtmp
 */
#ifndef PlayerCore_Thin
- (void) cloudCorePlayerRtcToRtmp;
#endif
/**
 播放器ip发生改变

 @param data ip地址
 */
- (void)cloudCorePlayerConnectionIpChanged:(NSString *)data;

/**
 收到dataChannel消息
 @param type 消息类型
 @param message 消息内容
 */
- (void)cloudCorePlayerReceiveDataChannel:(HMCloudCorePlayerDataChannelType)type message:(NSString *)message;

/**
 AudioSessionCategory发生改变
 @param category 当前音频模式
 */
- (void)cloudCorePlayerAudioSessionCategoryChanged:(HMCloudCorePlayerAudioSessionCategory)category;

/**
 inputWebsocket或者screenWebsocket断开
 @param socketType websoket类型
 */

- (void)cloudCorePlayerInputOrScreenSocketDisconned:(HMCloudCorePlayerWebsocketType)socketType;

/**
 RTC延迟信息回调
 @param delayModel 延迟信息
 */
- (void)cloudCorePlayerDelayInfoCallBack:(HMDelayInfoModel *)delayModel;

/**
 更新文件状态事件上报
 @param status 文件状态
 @param description 状态描述
 */
- (void)cloudCorePlayerStatusChanged:(CloudCorePlayerFileAction)status description:(NSString *)description;

/**
 DNS解析超时
 @param hostUrl 解析地址
 */
- (void)cloudCorePlayerDNSTimeout:(NSString *)hostUrl;

@end

@interface HMCloudCorePlayerViewController : UIViewController

@property (nonatomic, weak)   id<HMCloudCorePlayerViewControllerDelegate> delegate;
@property (nonatomic, assign) HMCloudCorePlayerOrientation playerOrientation;
@property (nonatomic, assign) BOOL isRotating;
@property (nonatomic, assign) CGSize showViewSize;
@property (nonatomic, assign) CGPoint showViewPoint;
@property (nonatomic, assign) BOOL resetPlayerFrame;
@property (nonatomic, assign) BOOL cameraPermissionCheck;
@property (nonatomic, assign) BOOL isVideoConnectSuccess;
@property (nonatomic, assign) BOOL isAudioConnectSuccess;
@property (nonatomic, assign) BOOL isInputConnectSuccess;
@property (nonatomic, assign) BOOL isScreenConnectSuccess;
@property (nonatomic, assign) BOOL stretching;
@property (nonatomic, assign) BOOL signalConnectSuccess;
@property (nonatomic, assign) BOOL receivedOffer;
@property (nonatomic, assign) BOOL sendAnswer;
@property (nonatomic, assign) BOOL sendCandidate;
@property (nonatomic, assign) BOOL receivedCandidate;
@property (nonatomic, assign) BOOL webrtcConnected;
@property (nonatomic, assign) BOOL isWebrtcConnectTimeout;


/**
 启动Player

 @param videoUrl 视频流地址，rtmp协议
 @param audioUrl 音频流地址，rtmp协议
 @param inputUrl 操作连接地址，ws协议
 @param screenUrl 音频输入连接地址，ws协议
 @param switchBRDuration 每次监测持续时间，单位秒
 @param detectInterval 采样时间间隔时间，单位毫秒
 @param delayTimeSinceLastCheck 相邻两帧间隔时间阀值，超过则通知帧延迟事件，单位毫秒
 @param switBRFrozenTime 发生自动切换后，不监测时间，单位秒
 @param isReportDetailInfo 是否上报延迟详细数据
 @param delaySampleInterval 延迟检测采样周期
 @param startDelayTime 起播后，不检测帧延迟时长，单位秒
 @param hidden 是否隐藏Player（池化重启，为true；池化非重启，非池化为false）
 @param websocketRetryMaxTimes WebSocket在sToken有效期内最大重连次数
 @param sdkStokenValidTime 对SDK的sToken有效期，单位秒
 @param notForceReconnect 不强行重连连接，等待streamer自己重连
 */
#ifndef PlayerCore_Thin
- (void) startPlayerWithVideoUrl:(NSString *)videoUrl
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
#endif
/**
 视频画面可见（池化重启）
 */
- (void) makePlayerVisible;

/**
 停止Player

 @param showLastFrame 是否显示最后一帧画面
 */
- (void) stopPlayer:(BOOL)showLastFrame;

/**
 停止Player

 @param showLastFrame 是否显示最后一帧画面
 */
- (void) stopPlayer:(BOOL)showLastFrame disconnectStream:(BOOL)disconnectStream;

/**
 停止Player
 @param showLastFrame 是否显示最后一帧画面
 @param disconnectStream 是否断流
 @param onlyPause 是否仅暂停游戏

 */
- (void) stopPlayer:(BOOL)showLastFrame disconnectStream:(BOOL)disconnectStream onlyPause:(BOOL)onlyPause;

/**
 播流过程中数据统计参数

 @param fpsInterval 帧数统计周期，单位秒
 @param bandInterval 流量统计周期，单位秒
 @param frameCount 流量统计周期内记录的最大Bytes帧数
 @param decodeInterval 平均解码耗时统计周期
 */
- (void) startStastics:(int)fpsInterval
          bandInterval:(int)bandInterval
            frameCount:(int)frameCount
        decodeInterval:(int)decodeInterval;

/**
 设置PingPong定时器参数

 @param interval 定时器周期
 @param delayTime 上报PingPong延迟上限，单位ms
 @param traceRouteTime 上报traceRout延迟上限，单位ms
 */
- (void) setPingPongParams:(NSInteger)interval
                 delayTime:(NSInteger)delayTime
            traceRouteTime:(NSInteger)traceRouteTime;

/**
基于Ping2Pong的时钟校准

@param interval 时钟校准间隔
@param reportDelayTimes 延迟信息上报间隔配置，单位s
*/

- (void) setPing2PongParams:(NSString *)interval ReportDelayTimes:(NSInteger)reportDelayTimes;

/**
webrtc 配置
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
 设置Player背景图

 @param image 背景图UIImage实例
 */
- (void) setBackgroundImage:(UIImage *)image;

/**
 前置同一层级view
 */
- (void) bringViewToFront:(UIView *)view;

/**
 添加子视图
*/
- (void) addSubView:(UIView *)view;

/**
 子视图居中
 */
- (void) centerSubView:(UIView *)view style:(HMCloudCorePlayerSubViewCenterStyle)style;

/**
 获取视频帧延迟

 @return 帧延迟，单位ms
 */
- (NSInteger) getVideoLatency;

/**
 发送按键

 @param keycode  keycode
 */
- (BOOL) sendKeycode:(NSInteger)keycode;

- (BOOL) sendCommand:(NSString *)cmd updateUserLastOperationTime:(BOOL)updateUserLastOperationTime;

- (BOOL) sendCommandForScreen:(NSString *)cmd;

- (BOOL) sendText:(NSString *)text;

/**
 设置Player是否静音
 @param mute 是否静音
 */
- (void) setPlayerAudioMute:(BOOL)mute;

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
 刷新SToken后重连inputUrl、screenUrl
 @param inputUrl  操作连接地址，ws协议
 @param screenUrl 小屏连麦长链接
 */
- (void)retryInputUrl:(NSString *)inputUrl screenUrl:(NSString *)screenUrl;

/**
 启动Webrtc Player
 @param model webrtc连接参数模型
 @param inputUrl 操作连接地址，ws协议
 @param screenUrl 小屏连麦长链接
 @param cid 实例id
 @param hidden 是否隐藏Player（池化重启，为true；池化非重启，非池化为false）
 @param notForceReconnect 不强行重连连接，等待streamer自己重连
 @param audioSessionCategory webrtc音频模式
 */

- (void)startPlayerWithWebrtcModel:(HMCloudPlayerWebRtcInfoModel *)model intputUrl:(NSString *)inputUrl screenUrl:(NSString *)screenUrl cid:(NSString *)cid hidden:(BOOL)hidden notForceReconnect:(BOOL)notForceReconnect audioSessionCategory:(HMCloudCorePlayerAudioSessionCategory)audioSessionCategory;


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
取消第一帧超时，rtc连接超时计时器
 */
- (void)cancelFirstArriveTimer;

/**
 获取webrtc版本号
 */
+ (NSString *)getWebRTCVersion;

/**
 RTMP推流参数设置
 @param probesize 数据量大小
 @param analyzeduration 时长
 @param fpsprobesize 帧大小
 @param framedropaudio 音频帧丢帧开关
 @param framedropvideo 视频帧丢帧开关
 @param config 108配置
 */
- (void)setRTMPProbesize:(int)probesize
         analyzeduration:(int)analyzeduration
            fpsprobesize:(int)fpsprobesize
          framedropaudio:(int)framedropaudio
          framedropvideo:(int)framedropvideo
                  config:(NSDictionary *)config;
/**
 展示本地键盘 如x86类型
 */
- (void)showPlayerKeyboard:(BOOL)isOpen;

/**
 展示全键盘
 */
- (void)showPlayerFullKeyboard:(BOOL)isOpen;

/**
 切换滑鼠模式
 */
- (void)switchMouseType:(HMCloudCorePlayerMouseType)mouseType;

/**
 获取滑鼠模式
 */
- (HMCloudCorePlayerMouseType)getMouseType;

/**
 启动Webrtc x86Player
 @param model webrtc X86连接参数模型
 @param resolutionSize 分辨率
 @param cid 实例id
 @param gamePadDict 游戏按键参数
 @param playWithControl 播放时是否带控制权
 @param pkgName 游戏包名
 @param userId 用户名
 @param audioSessionCategory webrtc音频模式
 */
- (void)startPlayerWithWebrtcX86Model:(HMCloudPlayerWebRtcInfoModel *)model cid:(NSString *)cid resolutionSize:(NSString *)resolutionSize gamePadDict:(NSDictionary *)gamePadDict playWithControl:(BOOL)playWithControl pkgName:(NSString *)pkgName userId:(NSString *)userId audioSessionCategory:(HMCloudCorePlayerAudioSessionCategory)audioSessionCategory;

/**
 切换手柄模式
 */
- (void)switchOperationType:(HMCloudCorePlayerOperationType)type;

/**
 获取手柄模式
 */
- (HMCloudCorePlayerOperationType)getOperationType;

/**
 是否开启x86手柄震动反馈
 @param isOpen BOOL yes 为开启 no为不开启
 */
- (void)openFeedback:(BOOL)isOpen;

/**
 获取反馈震动状态
 */
- (BOOL)getFeedback;

/**
 调节x86手柄透明度
 0-1    0为透明 1为不透明
 */
- (void)adjustCustomStickAlpha:(float)alpha;

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
 设置显示原始值
 @param isShow BOOL yes 为显示 no为不显示 默认不显示
 */
- (void)cloudSetShowVirtualRawKey:(BOOL)isShow;

/**
 编辑手柄
 @return yes 可以编辑 no 无法编辑
 */
- (BOOL)editOperationView;

- (void)enableFrameRenderedCallback:(BOOL)enabled;

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
- (void)getCloudImageList:(NSInteger)limit offset:(NSInteger)offset cloudFileImageListBlock:(HMCloudCorePlayerFileListBlock)cloudFileImageListBlock;

/**
 停止请求云图片列表计时器
 */
- (void)stopRequestCloudImageListTimer;

/**
 获取云游戏视频列表
 @param cloudFileVideoListBlock result 查询结果，videoList 视频列表 errorMsg result为NO时返回错误原因
 */
- (void)getCloudFileVideoList:(HMCloudCorePlayerFileListBlock)cloudFileVideoListBlock;

/**
 停止请求云视频列表计时器
 */
- (void)stopRequestCloudVideoListTimer;

/**
 停止播流（断流）
 */
- (void)stopPlayerStreaming;

/**
 获取游戏截屏
 @param scale 图片压缩比例,例如实例分辨率为  1280*720.  则scale传入“0.5”，收到的图片大小为640*360.
 @param responseBlock 图片返回结果
 */
- (void)captureScreenshot:(float)scale responseBlock:(CloudCorePlayerScreenshotBlock)responseBlock;

/**
 发送keycode值
 仅x86游戏使用
 */
- (BOOL)sendCustomKeycode:(HMInputOpData *)inputOPData;


/**
 将鼠标模式切换成pc模式
 仅x86游戏使用
 @param model true pc模式  false 移动端模式
 */
- (BOOL)convertToPcMouseModel:(BOOL)model;

/**
 拉伸画面
 @param stretch true 拉伸 false 还原
 */
- (void)setStretch:(BOOL)stretch;

/**
 开始录音
 */
- (void)startRecordX86;

/**
 停止录音
 */
- (void)stopRecordX86;

/**
 x86开始上传图片
 @param uploadList 上传列表
 @param cloudFileUploadResponseBlock 上传某一个文件回调
 @param cloudFileUploadComplete 上传完成回调
 @return 是否成功调用下载方法
 */

- (BOOL)upload:(NSArray<HMStreamingFileModel *> *)uploadList cloudFileUploadResponseBlock:(CloudCorePlayerUploadResponseBlock)cloudFileUploadResponseBlock cloudFileUploadComplete:(CloudCorePlayerUploadComplete)cloudFileUploadComplete;

- (BOOL)download:(NSArray<HMStreamingFileModel *> *)downloadList downloadProgress:(CloudCorePlayerDownloadProgressBlock)downloadProgressBlock responseBlock:(CloudCorePlayerDownloadResponseBlock)responseBlock completeBlock:(CloudCorePlayerDownloadComplete)completeBlock;

- (BOOL)cancelDownloadFileWithX86:(NSArray<HMStreamingFileModel *> *)fileList cloudFileCancelDownloadResponseBlock:(CloudCoreFileCancelDownloadResponseBlock)cancelDownloadResponseBlock cloudFileCancelDownloadComplete:(CloudCoreFileCancelDownloadComplete)cancelDownloadComplete;

- (BOOL)sendMessageToDataChannel:(CloudCorePlayerWSMessageType)messageType message:(NSString *)message;

- (NSString *)readClipboard;

- (void)writeContentToClipboard:(NSString *)content;
/**
 x86取消上传任务
 */
- (void)cancelUploadFileAllTask;

- (void)resetRetryPlayerTimes;

/**
 接收到dns解析后的ip
 @param dataDict ip地址
 */
- (void)receivedDNSIPData:(NSDictionary *)dataDict url:(NSString *)url;

/**
 切换键盘类型
 @param type 键盘类型
 @param completion 设置结果
 */
- (void)switchImeType:(CloudCorePlayerIMEType)type completion:(CloudCorePlayerIMECompletion)completion;

@end
