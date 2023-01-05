/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#import <Foundation/Foundation.h>
#import "RTCVideoRenderer.h"
#import "RTCDataChannel.h"

#import "ARDRtcMediaSettings.h"
#import "RTCMacros.h"

NS_ASSUME_NONNULL_BEGIN

RTC_EXTERN const NSString *kConfigKeyLog;               //是否打印日志 @YES or  @NO
RTC_EXTERN const NSString *kConfigKeyReconnect;         //是否自动重连 @YES or  @NO
RTC_EXTERN const NSString *kConfigKeyReconnectAttempts; //自动重连最大次数 NSNumber, int
RTC_EXTERN const NSString *kConfigKeyReconnectWait;     //自动重连等待时间 float 单位：s
RTC_EXTERN const NSString *kConfigKeyReconnectWaitMax;  //自动重连最大等待时间 float 单位：s
RTC_EXTERN const NSString *kConfigKeyConnectTimeout;    //Signal连接超时时间 float 单位：s
RTC_EXTERN const NSString *kConfigKeyForceWebsockets;   //是否强制使用weboskcets  @YES or  @NO

typedef NS_ENUM(NSInteger, RTC_OBJC_TYPE(ARDAppStreamType)) {
    kARDAppStreamTypeUnknown,
    kARDAppStreamTypeVideo,
    kARDAppStreamTypeAudio
};

typedef NS_ENUM(NSInteger, RTC_OBJC_TYPE(ARDAppClientStatus)) {
    kARDAppClientStatusInitialized,                 //initialized

    kARDAppClientStatusSignalConnecting,            //13215 WEBRTC开始连接/开始连接signalServer
    kARDAppClientStatusSignalConnected,             //13241 WEBRTC信令服务连接成功
    kARDAppClientStatusSignalFailed,                //13220 WEBRTC信令服务连接超时 socketIO默认超时
    kARDAppClientStatusSignalDisconnect,            //13242 WEBRTC信令服务连接断开
    kARDAppClientStatusSignalSendJoin,              //13221 WEBRTC发送join
    kARDAppClientStatusSignalRecvOffer,             //13222 WEBRTC收到offer
    kARDAppClientStatusPeerSetRemoteSDPFailed,      //13233 WEBRTC执行接口setRemoteDescription失败
    kARDAppClientStatusPeerCreateAnswer,            //13223 WEBRTC创建answer
    kARDAppClientStatusSignalSendAnswer,            //13224 WEBRTC发送answer
    kARDAppClientStatusPeerCreateAnswerFailed,      //13235 WEBRTC创建answer失败
    kARDAppClientStatusPeerSetLocalSDPSuccessed,    //13236 WEBRTC执行setLocalDescription成功
    kARDAppClientStatusPeerSetLocalSDPFailed,       //13237 WEBRTC执行setLocalDescription失败
    kARDAppClientStatusSignalRecvCandidate,         //13225 WEBRTC收到candidate
    kARDAppClientStatusPeerAddIceCandidate,         //13238 WEBRTC addIceCandidate 成功
    kARDAppClientStatusSignalSendCandidate,         //13226 WEBRTC发送candidate
    kARDAppClientStatusPeerConnected,               //13216 WEBRTC连接成功 pc成功
    kARDAppClientStatusPeerOnTrack,                 //13240 WEBRTC执行ontrack，获取流内容
    kARDAppClientStatusPeerDisconnected,            //13229 WEBRTC的client端PeerconnectionState=disconnected
    kARDAppClientStatusIceDisconnected,             //13228 WEBRTC的icefail iceConnectionState=disconnected
    kARDAppClientStatusPeerConnectionFailed,        //13450 peerconnectionFailed

    kARDAppClientStatusClosed,                      //13232 WEBRTC关闭close
    kARDAppClientStatusRereceive,                   //13248 WEBRTC收到第一帧
    kARDAppClientStatusDecoded,                     //13249 WEBRTC解码完第一帧
    kARDAPPClientStatusWebrtcConnectFail,           //13217 WEBRTC连接失败 pc失败

    kARDAPPClientStatusStartRequestSDP,             //13262 WEBRTC开始请求sdp
    kARDAPPClientStatusRequestSDPSuccess,           //13263 WEBRTC请求sdp成功
    kARDAPPClientStatusRequestSDPFail,              //13264 WEBRTC请求sdp失败
    kARDAPPClientStatusSetRemoteDescriptionFail,    //13265 WEBRTCsetRemoteDescription失败
    kARDAPPClientStatusDataChannelState,            //13266 WEBRTCDataChannelStateChanged
    kARDAPPClientStatusStartAskAnswer,              //13268 WEBRTC 开始askAnswer
    kARDAPPClientStatusAskAnswerSuccess,            //13269 WEBRTC askAnswer成功
    kARDAPPClientStatusAskAnswerFail,               //13270 WEBRTC askAnswer失败
};

//extern const NSString* ARDAppClientStatusString(RTC_OBJC_TYPE (ARDAppClientStatus) status);
RTC_EXTERN const NSString* ARDAppClientStatusString(RTC_OBJC_TYPE (ARDAppClientStatus) status);

typedef NS_ENUM(NSInteger, RTC_OBJC_TYPE(ARDAppClientErrorCode)) {
    //connectWithParameter 方法错误回调
    ARDAppClientErrorCodeParamErr               = 1001, //ARDAppClientParameter 参数无效
    ARDAppClientErrorCodeStatusErr,                     //非初始化完成/非关闭状态

    //signale server连接错误毁掉
    ARDAppClientErrorCodeSignalErr              = 2001,

    //coturn server连接错误回调
    ARDAppClientErrorCodeIceConnectionFailed    = 3001,

    //peer 连接错误回调
    ARDAppClientErrorCodePeerConnectionFailed   = 4001,
};

typedef NS_ENUM(NSInteger, RTC_OBJC_TYPE(ARDAppClientGameType)) {
    kARDAppClientGameTypeArm,
    kARDAppClientGameTypeX86,
};

@class RTC_OBJC_TYPE(ARDAppClient);
@class RTC_OBJC_TYPE(ARDAppClientParameter);
@class RTC_OBJC_TYPE(ARDAppClientDelayInfo);
@class RTC_OBJC_TYPE(ARDAppClientRtcConfig);
@class RTC_OBJC_TYPE(RTCCameraPreviewView);
@protocol RTC_OBJC_TYPE(ARDAppClientDelegate);

// Handles connections to the AppRTC server for a given room. Methods on this
// class should only be called from the main queue.
RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDAppClient) : NSObject

// If |shouldGetStats| is true, stats will be reported in 1s intervals through
// the delegate.
@property(nonatomic, assign) BOOL shouldGetStats;

@property(nonatomic, weak) id<RTC_OBJC_TYPE (ARDAppClientDelegate)> delegate;
@property(nonatomic, readonly) __kindof UIView<HMRTCVideoRenderer> *rtcVideoView;

// Convenience constructor since all expected use cases will need a delegate
// in order to receive remote tracks.
- (instancetype)initWithDelegate:(id<RTC_OBJC_TYPE (ARDAppClientDelegate)> __nullable)delegate;

- (instancetype)initWithDelegateAndControllog:(id<RTC_OBJC_TYPE (ARDAppClientDelegate)> __nullable)delegate enablelog:(bool)enablelog;
// Establishes a connection with the AppRTC servers for the given room id.
// |settings| is an object containing settings such as video codec for the call.
// If |isLoopback| is true, the call will connect to itself.
- (void)connectWithParameter:(RTC_OBJC_TYPE (ARDAppClientParameter) *)params rtcConfig:(RTC_OBJC_TYPE (ARDAppClientRtcConfig) *)rtcConfig cid:(NSString *)cid;

- (void)connectWithParameter:(RTC_OBJC_TYPE (ARDAppClientParameter) *)params globalConfig:(NSDictionary *)globalConfig cid:(NSString *)cid capturemode:(RTC_OBJC_TYPE (ARDRtcCaptureMode))mode gametype:(RTC_OBJC_TYPE (ARDAppClientGameType)) type;

// Disconnects from the AppRTC servers and any connected clients.
- (void)disconnect;

// Mute or unmute audio
- (void)mute:(BOOL)muted;

+ (NSString *)getWebRTCVersion;

- (void)enableFrameRenderedCallback:(BOOL)enabled;

- (UIImage *)getCurrentImage;

- (RTC_OBJC_TYPE (RTCDataChannel)*)createDataChannel:(NSString*)lable;

// for x86 to setting offer sdp
- (void) settingRemoteOfferSdp:(NSString*)sdp;
- (BOOL) sendDataWithDataBuffer:(NSData *)data withDC:(RTC_OBJC_TYPE (RTCDataChannel)*)dc;

// for media capture
- (void) setMicSettings:(RTC_OBJC_TYPE (ARDRtcMicSettings) *)settings;
- (void) setCameraSettings:(RTC_OBJC_TYPE (ARDRtcCameraSettings) *)settings;
- (void) setVideoEncoderSettings:(RTC_OBJC_TYPE (ARDRtcVideoEncoderSettings) *)settings;
- (void) setCameraPreviewView:(RTC_OBJC_TYPE(RTCCameraPreviewView)*)preview;
- (bool) startVideoCapture:(BOOL)is_portrait;
- (void) stopVideoCapture;
- (bool) switchCamera;
- (bool) startAudioCapture;
- (void) stopAudioCapture;

// for external pcm input
- (void) setExternalPcmFotmat:(int)samplerate channels:(int)channels;
- (void) deliverExternalPcmData:(int)size data:(uint8_t *)data;
@end

// The delegate is informed of pertinent events and will be called on the
// main queue.
RTC_OBJC_EXPORT
@protocol RTC_OBJC_TYPE (ARDAppClientDelegate) <NSObject>

/// ARDAppClient Status Changed callback, desp is error description if not nil
- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didChangeStatus:(RTC_OBJC_TYPE (ARDAppClientStatus))status withDesp:(NSString *)desp;

/// ARDAppClient ERROR callback,
- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didError:(NSError *)error;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didGetDelayInfo:(RTC_OBJC_TYPE (ARDAppClientDelayInfo) *)info;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didChangeVideoSize:(CGSize)size;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didStartRender:(RTC_OBJC_TYPE (ARDAppStreamType))type;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didUnusualMessage:(NSString *)message;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didCountlyEvent:(int)event desp:(NSString *)desp;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didRenderVideoFrame:(int64_t)timestamp;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didConnectionIpChanged:(NSString *)data;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didOpenDataChannel:(RTC_OBJC_TYPE (RTCDataChannel) *)dataChannel;

/// ARDAppClient x86 callback,
- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didCallBackToSaasSdk:(NSString *__nullable)sdp isaskoffer:(BOOL)isaskoffer;

- (void)appClient:(RTC_OBJC_TYPE (ARDAppClient) *)client didReceiveMessageWithBuffer:(RTC_OBJC_TYPE (RTCDataBuffer) *)buffer withDC:(RTC_OBJC_TYPE (RTCDataChannel)*)dc;

- (void)appClientDataChannelConnected;

@end

RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDAppClientParameter) : NSObject

/// ARDAppClientParameter Initializer
/// @param roomId  roomId
/// @param signalServerUrl  singnal server url
/// @param iceServerUrls  ice server uls
+ (instancetype) instanceWithRoomId:(NSString *)roomId
                    signalServerUrl:(NSString *)signalServerUrl
                      iceServerUrls:(NSArray<NSString *> *)iceServerUrls;

/// ARDAppClientParameter Initializer
/// @param roomId  roomId
/// @param signalServerUrl  singnal server url
/// @param iceServerUrls  ice server uls
/// @param config rtc configs
+ (instancetype) instanceWithRoomId:(NSString *)roomId
                    signalServerUrl:(NSString *)signalServerUrl
                      iceServerUrls:(NSArray<NSString *> *)iceServerUrls
                             config:(NSDictionary * __nullable)config;
@end

RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDAppClientDelayInfo) : NSObject
// 采集时间|网络耗时|解码耗时|渲染耗时|单帧耗时|采集延迟|帧大小|展示帧率|推流帧率|码率|乒乓耗时|感知延迟｜丢包率
//采集时间，Date.now()，ms
@property(nonatomic, assign) long long timestamp;
//网络耗时 candidate-pair    currentRoundTripTime
@property(nonatomic, assign) long long netDelayTime;
//解码耗时 ssrc / video    googDecodeMs
@property(nonatomic, assign) int decodeTime;
//渲染耗时 ssrc / video    googRenderDelayMs
@property(nonatomic, assign) int renderTime;
//单帧耗时 ssrc / video    googTargetDelayMs
@property(nonatomic, assign) long long frameDelayInfo;
//采集延迟 ssrc / video    googTargetDelayMs
@property(nonatomic, assign) long long nowDelayTime;
//帧大小 ssrc / video    bytesReceived (秒差)
@property(nonatomic, assign) long long recvFrameSize;
//展示帧率 ssrc / video    googFrameRateOutput
@property(nonatomic, assign) int renderFps;
//推流帧率 ssrc / video    googFrameRateReceived
@property(nonatomic, assign) int gameFps;
//码率 ssrc / video    bytesReceived (秒差)/s/8
@property(nonatomic, assign) long long bitRate;
//乒乓耗时 input ping-pong/2
@property(nonatomic, assign) long long pingPongTime;
//感知延迟 ssrc / video    googTargetDelayMs
@property(nonatomic, assign) long long targetDelayTime;
//丢包率 ssrc / video    packetsLost / packetsReceived  (PL2-PL1)/((PL2-PL1)+(PR2-PR1))
@property(nonatomic, assign) float packetsLostRate;

#pragma mark - more debug info
//分辨率
@property(nonatomic, readonly)  NSString *resolution;
//content type // googContentType
@property(nonatomic, copy)      NSString *contentType;
//currentDelay // googCurrentDelayMs
@property(nonatomic, assign)    long long currentDelay;
//解码帧率，googFrameRateDecoded
@property(nonatomic, assign)    long long decodeFps;
//jitterBufferMs //googJitterBufferMs
@property(nonatomic, assign)    long long jitterBufferMs;
//nacksSent //googNacksSent
@property(nonatomic, assign)    long long nacksSent;
//丢包数 //packetsLost
@property(nonatomic, assign)    long long packetsLost;

#pragma mark - debug ext info
//编码格式 //googCodecName VP8
@property (nonatomic, copy)     NSString *fmt;

//编解码名称 //codecImplementationName
@property (nonatomic, copy)     NSString *codecName;

//首帧耗时 //googFirstFrameReceivedToDecodedMs
@property (nonatomic, assign)   long long ffTime;

- (NSString *) detailInfo;

@end

RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDAppClientRtcConfig) : NSObject
@property (nonatomic, copy) NSNumber *dropClientCandidate;
@property (nonatomic, copy) NSNumber *connPingIntervalMs;
@property (nonatomic, copy) NSNumber *unWritableTimeoutMs;
@property (nonatomic, copy) NSNumber *unWritableMinChecks;
- (instancetype)initWithDropClientCandidate:(NSNumber *)dropClientCandidate
                         connPingIntervalMs:(NSNumber *)connPingIntervalMs
                        unWritableTimeoutMs:(NSNumber *)unWritableTimeoutMs
                        unWritableMinChecks:(NSNumber *)unWritableMinChecks;
@end

NS_ASSUME_NONNULL_END
