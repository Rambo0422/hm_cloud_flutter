//
//  HMCloudPlayerRecordEvent.h
//  HMCloudPlayerCore
//
//  Created by Apple on 2018/6/13.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>
@class HMCCEvent;

typedef NS_ENUM(NSInteger, HMCPEventType) {
    HMCEventInitCalled                  = 12058,    //开始调用初始化方法
    HMCEventInitFailed                  = 12066,    //初始化方法调用失败 --初始化相关事件，持久化保存并上报
    HMCEventGetDidStarted               = 12063,    //开始调用getDid方法
    HMCEventGetDidSuccess               = 12078,    //调用getDid方法成功
    HMCEventGetDidFailed                = 12079,    //调用getDid方法失败

    HMCEventGetConfigureStarted         = 12084,    //开始调用getConfig方法
    HMCEventGetConfigureSuccess         = 12080,    //调用getConfig方法成功
    HMCEventGetConfigureFailed          = 12081,    //调用getConfig方法失败

    HMCEventStartPlay                   = 10012,    //开始启动云玩

    HMCEventTitleVideoDownloadStarted   = 12024,    //片头开始预加载
    HMCEventTitleVideoDownloadSuccess   = 12025,    //片头预加载成功
    HMCEventTitleVideoDownloadFailed    = 12026,    //片头预加载失败

    HMCEventTitleVideoPlayStarted       = 12027,    //开始播放片头
    HMCEventTitleVideoPlaySuccess       = 12028,    //片头播放成功
    HMCEventTitleVideoPlayFailed        = 12029,    //片头播放失败

    HMCEventSpeedTestStarted            = 12030,    //开始测速
    HMCEventSpeedTestSuccess            = 12031,    //测速完成
    HMCEventSpeedTestFailed             = 12032,    //测速失败
    HMCEventBandwidthTooLow             = 12046,    //低于服务下限

    /*
     HMCEventGetCidStarted               = 12082,    //开始调用getCid方法
     HMCEventGetCidSuccess               = 12072,    //调用getCid方法成功
     HMCEventGetCidFailed                = 12073,    //调用getCid方法失败
     */

    HMCEventGetCidV2Started             = 12120,    //开始 getCloudServiceV2 调用
    HMCEventGetCidV2Success             = 12121,    //调用getCloudServiceV2 成功
    HMCEventGetCidV2Failed              = 12122,    //调用getCloudServiceV2 失败

    HMCEventSaasWSConnectStarted        = 12088,    //SaaS-WS连接开始建立
    HMCEventSaasWSConnectSuccess        = 12089,    //SaaS-WS连接成功
    HMCEventSaasWSDisconnected          = 12090,    //SaaS-WS连接断开 失败错误码
    HMCEventSaasWSConnectFailed         = 12050,    //SaaS-WS连接/重连失败

    HMCEventGetCloudServiceStarted      = 12083,    //开始调用getCloudService方法
    HMCEventGetCloudServiceSuccess      = 12074,    //调用getCloudService方法成功
    HMCEventGetCloudServiceFailed       = 12075,    //调用getCloudService方法失败    失败错误码
    HMEventGetReservedInctanceStarted   = 12093,    //开始调用检查是否有驻留实例方法
    HMEventGetReservedInctanceSuccess   = 12094,    //调用检查是否有驻留实例方法成功
    HMEventGetReservedInctanceFailed    = 12095,    //调用检查是否有驻留实例方法失败
    HMEventReleaseInstanceStarted       = 12201,    //开始调用根据cid立即释放实例
    HMEventReleaseInstanceSuccess       = 12202,    //根据cid立即释放实例成功
    HMEventReleaseInstanceFail          = 12203,    //根据cid立即释放实例失败

    HMCEventSaaSWSMessage               = 12054,    //收到SaaS-WS消息,WS下发的: 消息类型,消息内容
    HMCEventStreamInfoGot               = 12044,    //第一次收到流地址时上报

    HMCEventInputConnectStarted         = 12085,    //input开始建立链接
    HMCEventInputConnectSuccess         = 10021,    //input操作连接成功
    HMCEventInputConnectFailed          = 10022,    //input操作连接失败
    HMCEventInputDisconnected           = 12049,    //Input连接断开

    HMCEventScreenConnectURLInvalid     = 12400,    //screenUrl为空
    HMCEventScreenConnectStarted        = 12401,    //screenUrl开始建立链接
    HMCEventScreenConnectSuccess        = 12402,    //screenUrl连接成功
    HMCEventScreenDisconnected          = 12403,    //screenUrl连接断开
    HMCEventScreenConnectAgain          = 12404,    //screenUrl重新建立链接

    HMCEventAudioConnectStarted         = 12086,    //音频流连接开始建立
    HMCEventAudioConnectSuccess         = 12033,    //流链接成功（音频部分）
    HMCEventAudioConnectFailed          = 12035,    //流链接失败（音频部分）

    HMCEventVideoConnectStarted         = 12087,    //视频流连接开始建立
    HMCEventVideoConnectSuccess         = 12034,    //流链接成功（视频部分）
    HMCEventVideoConnectFailed          = 12036,    //流链接失败（视频部分）
    HMCEventVideoPlayRate               = 12039,    //播流启动时，当前码率上报，测速起播、切换码率--包含扩展参数，数据为码率值
    HMCEventVideoFirstFrame             = 12045,    //获取到视频第一帧

    HMCEventInputPingoongDelayTime      = 10050,    //“播流期间每固定周期input ping/pong时间” -- ping-pong间隔时间(ms)
    HMCEventInputPingoongDelayOverload  = 10033,    //每ping-pong间隔达到阀值上报, ping-pong间隔时间(ms)
    HMCEventInputPingoongDelayTraceroute= 10051,    //每ping-pong间隔达到阀值，上报traceroute”

    HMCEventVideoFrameDelayOverload     = 12038,    //播流期间，每帧间隔时间达到阀值上报
    HMCEventVideoFrameDelayPeriodResult = 12042,    //每固定周期帧延迟结果上报
    HMCEventVideoDecodeTimePeriodResult = 12037,    //播流期间每固定周期每帧解码平均耗时

    HMCEventManualResolutionStarted     = 12014,    //开始手动切换,当前码率Id:当前码率值,目标码率Id:目标码率值
    HMCEventManualResolutionSuccess     = 12015,    //手动切换成功,当前码率Id:当前码率值,目标码率Id:目标码率值,切换耗时(ms)
    HMCEventManualResolutionFailed      = 12016,    //手动切换失败,当前码率Id:当前码率值,目标码率Id:目标码率值,失败原因

    HMCEventAutoResolutionStarted       = 12011,    //开始自动切换
    HMCEventAutoResolutionSucceed       = 12012,    //自动切换成功,当前码率Id:当前码率值,目标码率Id:目标码率值,切换耗时(ms)
    HMCEventAutoResolutionFailed        = 12013,    //自动切换失败,当前码率Id:当前码率值,目标码率Id:目标码率值,失败原因

    HMCEventStopCloudServiceStarted     = 12091,    //开始调用stopCloudService方法
    HMCEventStopCloudServiceSuccess     = 12076,    //调用stopCloudService方法成功
    HMCEventStopCloudServiceFailed      = 12077,    //调用stopCloudService方法失败

    HMCEventStopCalled                  = 12053,    //stop方法被调用  调用类型 0:App 1:SDK

    HMCEventWIFI2Mobile                 = 12017,    //检测到wifi进入移动网络环境
    HMCEventMobile2WIFI                 = 12018,    //检测到移动网络环境进入wifi环境
    HMCEventMobileConfirm               = 12055,    //进入移动环境后，用户选择继续

    HMCEventAppForceground              = 12040,    //播流应用进入前台
    HMCEventAppBackground               = 12041,    //播流应用进入后台

//    HMCEventStasticInfoReport           = 12299,    //配合App性能参数上报

    HMCEventQueueConfirm                = 12019,    //用户选择继续
    HMCEventQueueCancel                 = 10039,    //用户取消入队

    HMCEventGameStopped                 = 13053,    //云游戏结束

    HMCEventIPV4QueryStarted            = 12130,    //开始调用 获取ipv4方法
    HMCEventIPV4QuerySuccess            = 12131,    //获取ipv4成功
    HMCEventIPV4QueryFailed             = 12132,    //获取ipv4失败

//    HMCEventTimeCalibrateDelayTime      = 16001,    //基于时钟校准后的延迟检测
    HMCEventPhoenix2DelayTime           = 16003,    //凤凰2.0延迟信息上报

    HMCCEventSignalConnecting           = 13215,    //WEBRTC开始连接/开始连接signalServer
    HMCCEventPeerConnected              = 13216,    //WEBRTC连接成功 pc成功
    HMCCEventPeerConnectionFailed       = 13450,    //PeerConnectionFailed
    HMCCEventSignalFailed               = 13220,    //WEBRTC信令服务链接超时 socketIO默认超时
    HMCCEventSignalSendJoin             = 13221,    //WEBRTC发送join
    HMCCEventSignalRecvOffer            = 13222,    //WEBRTC收到offer
    HMCCEventPeerCreateAnswer           = 13223,    //WEBRTC创建answer
    HMCCEventSignalSendAnswer           = 13224,    //WEBRTC发送answer
    HMCCEventSignalRecvCandidate        = 13225,    //WEBRTC收到candidate
    HMCCEventSignalSendCandidate        = 13226,    //WEBRTC发送candidate
    HMCCEventPeerDisconnected           = 13229,    //WEBRTC的client端PeerconnectionState=disconnected
    HMCCEventIceDisconnected            = 13228,    //WEBRTC的client端Peer iceConnectionState=disconnected
    HMCCEventReconnect                  = 13231,    //WEBRTC重连
    HMCCEventClosed                     = 13232,    //WEBRTC关闭close
    HMCCEventPeerSetRemoteSDPFailed     = 13233,    //WEBRTC执行接口setRemoteDescription失败
    HMCCEventPeerCreateAnswerFailed     = 13235,    //WEBRTC创建answer失败
    HMCCEventPeerSetLocalSDPSuccessed   = 13236,    //WEBRTC执行setLocalDescription成功
    HMCCEventPeerSetLocalSDPFailed      = 13237,    //WEBRTC执行setLocalDescription失败
    HMCCEventPeerOnTrack                = 13240,    //WEBRTC执行ontrack，获取流内容
    HMCCEventSignalConnected            = 13241,    //WEBRTC信令服务连接成功
    HMCCEventPeerAddIceCandidate        = 13238,    //WEBRTC addIceCandidate 成功
    HMCCEventSignalDisconnect           = 13242,    //WEBRTC信令服务连接断开
    HMCCEventCodecType                  = 13246,    //codec解码格式

    HMCCEventStartLivingStarted         = 13301,    //请求直播
    HMCCEventStartLivingSuccess         = 13302,    //请求直播成功
    HMCCEventStartLivingFailed          = 13303,    //请求直播失败

    HMCCEventStopLivingStarted          = 13304,    //请求停止直播
    HMCCEventStopLivingSuccess          = 13305,    //请求停止直播成功
    HMCCEventStopLivingFailed           = 13306,    //请求停止直播失败

    HMCCEventAuthCodeStarted            = 13307,    //请求授权码
    HMCCEventAuthCodeSuccess            = 13308,    //请求授权码返回成功
    HMCCEventAuthCodeFailed             = 13309,    //请求授权码返回失败

    HMCCEventGetControlCidStarted       = 13310,    //请求控制权
    HMCCEventGetControlCidSuccess       = 13311,    //请求控制权成功
    HMCCEventGetControlCidFailed        = 13312,    //请求控制权失败
    HMCCEventGetControlSuccess          = 13313,    //通知获得控制权成功
    HMCCEventGetControlFailed           = 13314,    //通知获得控制权失败
    HMCCEventLoseControl                = 13315,    //通知失去控制权
    HMCCEventSwitchStreamInfo           = 13801,    //226请求
    HMCCEventSwitchStreamInfoSuccess    = 13802,    //226请求成功
    HMCCEventSwitchStreamInfoFailed     = 13803,    //226请求失败
    HMCCEventAnalyseLog                 = 12200,    //分析问题log
    HMCCEventRereceive                  = 13248,    //WEBRTC收到第一帧
    HMCCEventDecoded                    = 13249,    //WEBRTC解码完第一帧
    HMCCEventWebrtcConnectionFailed     = 13217,    //WEBRTC连接失败
    HMCCEventCloudServiceParamVerifyFail= 12232,    //请求getCloudService时参数校验不正确
    HMCCEventPrepareParamVerifyFail     = 12233,    //准备游戏参数校验不正确
    HMCCEventPlayParamVerifyFail        = 12235,    //play游戏时检验参数不正确
    HMCCEventSaaSInvaildWSMessage       = 12230,    //saas下发信息不正确
    HMCCEventReportDelayInfoFailed      = 12234,    //延迟上报未达到条件无法上报16003

    HMCCEventWSMessageStarted           = 12901,   //开始发送WS消息
    HMCCEventWSMessageSuccessed         = 12902,   //WS消息发送成功
    HMCCEventWSMessageFailed            = 12903,   //WS消息发送失败
    HMCCEventWSMessageReceived          = 12904,   //收到WS消息
    HMCCEventPlayerIpChanged            = 13320,   //通知app端推流streamer ip发生变化
    HMCCEventCreateOfferFailed          = 13261,   //13261 WEBRTC创建offer失败
    HMCCEventStartRequestSDP            = 13262,   //13262 WEBRTC开始请求sdp
    HMCCEventRequestSDPSuccess          = 13263,   //13263 WEBRTC请求sdp成功
    HMCCEventRequestSDPFail             = 13264,   //13264 WEBRTC请求sdp失败
    HMCCEventSetRemoteDescriptionFail   = 13265,   //13265 WEBRTCsetRemoteDescription失败
    HMCCEventDataChannelState           = 13266,   //13266 WEBRTCDataChannelStateChanged
    HMCCEventGSMNotificationGameOver    = 13266,   //13267 GSMNotification结束游戏
    HMCCEventStartQueryAssignControl    = 12250,   //x86开始查询控制权
    HMCCEventQueryAssignControlSuccess  = 12251,   //x86查询控制权成功
    HMCCEventQueryAssignControlFail     = 12252,   //x86查询控制权失败
    HMCCEventStartAssignControl         = 12253,   //x86开始分配控制权
    HMCCEventAssignControlSuccess       = 12254,   //x86分配控制权成功
    HMCCEventAssignControlFail          = 12255,   //x86分配控制权失败
    HMCCEventStartCapture               = 12260,   //x86开始截图
    HMCCEventStartCaptureSuccess        = 12261,   //x86截图成功
    HMCCEventStartCaptureFail           = 12262,   //x86截图失败
    HMCCEventKeyboardStatusChanged      = 12701,   //键盘状态变化
    HMCCEventStartAskAnswer             = 13268,   //13268 WEBRTC 开始askAnswer
    HMCCEventAskAnswerSuccess           = 13269,   //13269 WEBRTC askAnswer成功
    HMCCEventAskAnswerFail              = 13270,   //13270 WEBRTC askAnswer失败
    HMCEventPreStreamInfoGot            = 12241,   //收到op4.5
    HMCEventFinalStreamInfoGot          = 12242,   //收到op5

    HMCCEventDownloadStart              = 12611,   //开始下载
    HMCCEventDownloadSuccess            = 12612,   //文件下载成功
    HMCCEventDownloadStop               = 12613,   //文件下载停止
    HMCCEventDownloadCancel             = 12614,   //文件取消下载
    HMCCEventDownloadError              = 12615,   //下载错误
    HMCCEventDownloadComplete           = 12616,   //下载完成
    HMCCEventDownloadProgress           = 12617,   //下载状态

    HMCCEventWebrtcDCCreateDevice       = 13455,   //WEBRTC DataChannel创建device
    HMCCEventWebrtcDCReceiveFirstBinary = 13456,   //WEBRTC DataChannel当收第一个二进制指令时上报，且仅上报一次
    HMCCEventWebrtcDCReceiveFirstText   = 13457,   //WEBRTC DataChannel当收第一个文本指令时上报，且仅上报一次
    HMCCEventWebrtcDCSendFirstBinary    = 13458,   //WEBRTC DataChannel发送第一个二进制数据包时上报
    HMCCEventWebrtcDCSendFirstText      = 13459,   //WEBRTC DataChannel发送第一个文本指令时上报，且仅上报一次
    HMCCEventWebrtcDCCloseDevice        = 13460,   //WEBRTC DataChannel device关闭
    HMCCEventCameraNotPermission        = 13470,   //相机权限未授予
    HMCCEventCameraApplyPermission      = 13471,   //正在申请相机权限
    HMCCEventCameraPermissionCheck      = 13472,   //是否需要每次对相机权限进行授权
    HMCCEventOperatorChange             = 17001,   //运营商发生变化
};

@interface HMCloudPlayerRecordEvent : NSObject

+ (void) recordEvent:(NSInteger)event accessKeyId:(NSString *)accessKeyId;
+ (void) recordEvent:(NSInteger)event accessKeyId:(NSString *)accessKeyId callback:(void (^)(BOOL successed, HMCCEvent *event))callback;

+ (void) recordEvent:(NSInteger)event accessKeyId:(NSString *)accessKeyId eventData:(NSString *)eventData;
+ (void) recordEvent:(NSInteger)event accessKeyId:(NSString *)accessKeyId eventData:(NSString *)eventData  callback:(void (^)(BOOL successed, HMCCEvent *event))callback;

+ (void) recordEvent:(NSInteger)event accessKeyId:(NSString *)accessKeyId eventData:(NSString *)eventData time:(long long)timeStamp;

+ (void) recordCachedEvent:(HMCCEvent *)event callback:(void (^)(BOOL successed, HMCCEvent *event))callback;

@end
