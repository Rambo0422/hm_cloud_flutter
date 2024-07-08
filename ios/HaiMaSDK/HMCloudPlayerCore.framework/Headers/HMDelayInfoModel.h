//
//  HMDelayInfoModel.h
//  HMCloudCore
//
//  Created by apple on 2019/11/28.
//  Copyright © 2019 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HMDelayInfoModel : NSObject

/// 采集时间
@property(nonatomic,assign) long long collectTime;
/// 网络耗时
@property(nonatomic,assign) long long netDelayTime;
/// 解码耗时
@property(nonatomic,assign) int decodeDelayTime;
/// 渲染耗时
@property(nonatomic,assign) int renderDelayTime;
/// 单帧耗时
@property(nonatomic,assign) long long aFrameDelayTime;
/// 采集延迟
@property(nonatomic,assign) long long nowDelayTime;
/// 帧大小
@property(nonatomic,assign) long long frameNowSize;
/// 展示帧率
@property(nonatomic,assign) float showFps;
/// 推流帧率
@property(nonatomic,assign) int gameFps;
/// 码率
@property(nonatomic,assign) long long bitRate;
/// 乒乓耗时
@property(nonatomic,assign) long long pingpongCostTime;
/// 丢包率
@property(nonatomic, assign) float packetLostPercent;
/// jitter buffer中的延迟时间
@property(nonatomic, assign) long long jitterBufferMs;
/// 编码耗时单位ms
@property(nonatomic, assign) int encoderDelayTime;
/// fec 保护率
@property(nonatomic, assign) float fecPacketsPercent;
/// fec 恢复率
@property(nonatomic, assign) float fecRecoveredPercent;
/// 卡顿次数
@property(nonatomic, assign) int jankCount;
/// 卡顿时长
@property(nonatomic, assign) int jankDuration;
/// pli个数
@property(nonatomic, assign) long pliSent;
/// 本地和远端的系统时间差
@property(nonatomic, assign) int remoteToLocalClockTimeOffset;
/// 音视频码率之和，单位是字节
@property(nonatomic, assign) long totalBitrate;
/// 统计周期内的freeze次数
@property(nonatomic, assign) int freezeCount;
/// 解码器输出的帧间隔方差（帧间隔稳定度)
@property(nonatomic, assign) int decodeVariance;
/// 显示模块的帧间隔方差（帧间隔稳定度)
@property(nonatomic, assign) int renderVariance;
/// 卡顿时长(包括Jank和BigJank，单位ms)
@property(nonatomic, assign) int jankAndFreezeDuration;
/// 解码帧率
@property(nonatomic, assign) long realFrameRateDecode;
/// 实际的显示帧率
@property(nonatomic, assign) int frameRateEglRender;
/// 音频接收码率，单位是字节
@property(nonatomic, assign) long bitrateAudio;
/// 解码输出的音频数据的音量level
@property(nonatomic, assign) int audioLevel;
/// 编码输入帧率
@property(nonatomic, assign) long videoInputFps;
/// 发送帧率
@property(nonatomic, assign) long videoSendFps;
/// 发送码率，单位是kbps
@property(nonatomic, assign) long videoSendKbps;


- (instancetype)initWithDelayInfo:(long long)collectTime NetDelayTime:(long long)netDelayTime DecodeDelayTime:(int)decodeDelayTime RenderDelayTime:(int)renderDelayTime AFrameDelayTime:(long long)aFrameDelayTime NowDelayTime:(long long)nowDelayTime FrameNowSize:(long long)frameNowSize ShowFps:(float)showFps GameFps:(int)gameFps BitRate:(long long)bitRate PingPongCostTime:(long long)pingPongCostTime PacketLostPercent:(float)packetLostPercent JitterBufferMs:(long long)jitterBufferMs EncoderDelayTime:(int)encoderDelayTime;

@end
