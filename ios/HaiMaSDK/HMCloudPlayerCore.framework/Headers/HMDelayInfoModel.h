//
//  HMDelayInfoModel.h
//  HMCloudCore
//
//  Created by apple on 2019/11/28.
//  Copyright © 2019 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HMDelayInfoModel : NSObject

@property(nonatomic,assign) long long collectTime;
@property(nonatomic,assign) long long netDelayTime;
@property(nonatomic,assign) int decodeDelayTime;
@property(nonatomic,assign) int renderDelayTime;
@property(nonatomic,assign) long long aFrameDelayTime;
@property(nonatomic,assign) long long nowDelayTime;
@property(nonatomic,assign) long long frameNowSize;
@property(nonatomic,assign) float showFps;
@property(nonatomic,assign) int gameFps;
@property(nonatomic,assign) long long bitRate;
@property(nonatomic,assign) long long pingpongCostTime;
//丢包率
@property(nonatomic, assign) float packetLostPercent;
//jitter buffer中的延迟时间
@property(nonatomic, assign) long long jitterBufferMs;
//编码耗时单位ms
@property(nonatomic, assign) int encoderDelayTime;

- (instancetype)initWithDelayInfo:(long long)collectTime NetDelayTime:(long long)netDelayTime DecodeDelayTime:(int)decodeDelayTime RenderDelayTime:(int)renderDelayTime AFrameDelayTime:(long long)aFrameDelayTime NowDelayTime:(long long)nowDelayTime FrameNowSize:(long long)frameNowSize ShowFps:(float)showFps GameFps:(int)gameFps BitRate:(long long)bitRate PingPongCostTime:(long long)pingPongCostTime PacketLostPercent:(float)packetLostPercent JitterBufferMs:(long long)jitterBufferMs EncoderDelayTime:(int)encoderDelayTime;

@end
