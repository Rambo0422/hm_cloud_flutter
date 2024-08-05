//
//  HmCloudTool.h
//  hm_cloud
//
//  Created by a水 on 2024/7/31.
//

#import <Foundation/Foundation.h>
#import <HMCloudPlayerCore/HMCloudPlayer.h>
NS_ASSUME_NONNULL_BEGIN

/// MARK: call method
static NSString *MethodStart = @"startCloudGame";
static NSString *MethodExitQueue = @"exitQueue";


/// MARK: action name
static NSString *ActionExitGame = @"exitGame";
static NSString *ActionQueueInfo = @"queueInfo";

@protocol HmCloudToolDelegate <NSObject>

- (void)sendToFlutter:(NSString *)actionName params:(id _Nullable)params;

@end


@interface HmCloudTool : NSObject <HMCloudPlayerDelegate>


/// 渠道id
@property (nonatomic, strong)   NSString *accessKeyId;

/// 用户userId
@property (nonatomic, strong)   NSString *userId;

/// 用户userToken
@property (nonatomic, strong)   NSString *userToken;

/// 渠道名
@property (nonatomic, strong)   NSString *channelName;

/// 游戏id
@property (nonatomic, strong)   NSString *gameId;

/// 游戏名
@property (nonatomic, strong)   NSString *gameName;

/// 游戏包名
@property (nonatomic, strong)   NSString *gamePkName;

/// 海马需要用的ctoken
@property (nonatomic, strong)   NSString *cToken;

/// 总时长
@property (nonatomic, strong)   NSNumber *playTime;

/// 高峰时长
@property (nonatomic, strong)   NSNumber *peakTime;

/// 当前队列级别
@property (nonatomic, strong)   NSNumber *priority;

/// 是否使用的高峰通道
@property (nonatomic, assign)   BOOL isPeakChannel;

/// vip到期时间
@property (nonatomic, strong)   NSNumber *vipExpiredTime;

/// 当前服务器时间
@property (nonatomic, strong)   NSNumber *realTime;


@property (nonatomic, weak) id<HmCloudToolDelegate> delegate;

+ (instancetype)share;
- (void)configWithParams:(NSDictionary *)params;
- (void)registWithDelegate:(id<HmCloudToolDelegate>)delegate;

- (void)stop;

@end

NS_ASSUME_NONNULL_END
