//
//  HMCCPayloadData.h
//  HMCloudCore
//
//  Created by Apple on 2018/5/21.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import "HMCCBaseModel.h"
#import "HMCloudPlayerWebRtcInfoModel.h"
#import "HMCloudPlayerResolution.h"

@class HMCCPayloadQueueItem;
@class HMCloudPlayerResolution;

@interface HMCCPayloadData : HMCCBaseModel

@property (nonatomic, copy)     NSString    *errorCode;
@property (nonatomic, copy)     NSString    *errorMsg;

//排队相关
@property (nonatomic, assign)   NSInteger   time;
@property (nonatomic, assign)   NSInteger   index;
@property (nonatomic, assign)   NSInteger   waitTotalNum;
@property (nonatomic, copy)     NSString    *timeStr;

//多队列排队
@property (nonatomic, strong)   NSArray<NSDictionary *> *queues;
@property (nonatomic, strong)   NSArray<HMCCPayloadQueueItem *> *queueItems;

//推流地址
@property (nonatomic, copy)     NSString    *videoUrl;
@property (nonatomic, copy)     NSString    *audioUrl;
@property (nonatomic, copy)     NSString    *inputUrl;
@property (nonatomic, copy)     NSString    *screenUrl;
@property (nonatomic, copy)     NSString    *resolution;

@property (nonatomic, copy)     NSString    *idcId;
@property (nonatomic, copy)     NSString    *idcName;
@property (nonatomic, copy)     NSDictionary *sdkDisplayInfo;

//游戏时长
@property (nonatomic, copy)     NSString    *formatter;
@property (nonatomic, assign)   NSInteger   ahead;
@property (nonatomic, assign)   BOOL        countDown;
@property (nonatomic, copy)     NSString    *tip;
@property (nonatomic, assign)   BOOL        finishByServer;
@property (nonatomic, copy)     NSString    *finishTip;
@property (nonatomic, assign)   NSInteger   playingTime;

//维护相关
@property (nonatomic, strong)   NSString    *endTimeText;

//流类型
@property (nonatomic, strong)   NSString    *streamType;

//rtc流地址
@property (nonatomic,strong)    NSDictionary *webRtcInfo;
@property (nonatomic,strong)    HMCloudPlayerWebRtcInfoModel *webRtcInfoModel;

//转授权相关
@property (nonatomic, strong)   NSString    *masterId;
@property (nonatomic, strong)   NSString    *pin;
@property (nonatomic, strong)   NSString    *followerCid;
@property (nonatomic, strong)   NSString    *uId;
@property (nonatomic, assign)   int         supportLiving;
@property (nonatomic, strong)   NSString    *stateChangeReason;
@property (nonatomic, copy)     NSString    *gsmUrl;
@property (nonatomic, strong)   NSDictionary *resolutionInfo;
@property (nonatomic, strong)   HMCloudPlayerResolution *x86Resolution;
@property (nonatomic, assign)   BOOL         reconnectStream;

+ (instancetype) instanceWithDictionary:(NSDictionary *)dict errorCode:(NSString *)errorCode errorMsg:(NSString *)errorMsg;

- (NSArray *) getQueueItemArray;

@end

@interface HMCCPayloadQueueItem : HMCCBaseModel

@property (nonatomic, assign)   NSInteger   rank;
@property (nonatomic, assign)   NSInteger   index;
@property (nonatomic, assign)   NSInteger   time;
@property (nonatomic, copy)     NSString    *timeStr;
@property (nonatomic, strong)   NSArray     *priorities;

- (NSDictionary *) queueItemDictionary;

@end
