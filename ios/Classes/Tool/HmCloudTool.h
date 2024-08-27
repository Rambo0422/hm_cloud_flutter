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
static NSString *MethodClosePage = @"closePage";


/// MARK: action name
static NSString *ActionExitGame = @"exitGame";
static NSString *ActionQueueInfo = @"queueInfo";
static NSString *ActionOpenPage = @"openPage";
static NSString *ActionClosePage = @"closePage";

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

// 上号助手相关
@property (nonatomic, strong)   NSString *account;
@property (nonatomic, strong)   NSString *password;
@property (nonatomic, strong)   NSString *platform_game_id;
@property (nonatomic, strong)   NSString *key;
@property (nonatomic, strong)   NSString *accountToken;
@property (nonatomic, strong)   NSString *accountGameid;
@property (nonatomic, strong)   NSString *accountUserid;
@property (nonatomic, strong)   NSString *platform;


/// 按键震动
@property (nonatomic, assign)   BOOL isVibration;

/// 云游互动
@property (nonatomic, assign)   BOOL isLiving;

/// 鼠标模式
@property (nonatomic, assign)   HMCloudCoreTouchMode touchMode;

/// 鼠标灵敏度
@property (nonatomic, assign)   float sensitivity;

@property (nonatomic, strong)             NSString *cloudId;
@property (nonatomic, strong)             NSString *liveRoomId;


@property (nonatomic, weak) id<HmCloudToolDelegate> delegate;

+ (instancetype)share;
- (void)configWithParams:(NSDictionary *)params;
- (void)registWithDelegate:(id<HmCloudToolDelegate>)delegate;

// 是否是vip
- (BOOL)isVip;

- (void)stop;
/// 推出flutter 页面后，回到游戏页面的方法
- (void)restart;

- (void)startLiving;
- (void)stopLiving;

/// 发送指令到海马
/// - Parameter dictList: HMOneInputOPData json的集合
- (void)sendCustomKey:(NSArray<NSDictionary *> *)dictList;


/// 更新鼠标模式
/// - Parameter touchMode: [HMCloudCoreTouchMode]
- (void)updateTouchMode:(HMCloudCoreTouchMode)touchMode;


/**
   sensitivity 0-1 0 鼠标无法移动 1 鼠标正常移动
 */
- (void)updateMouseSensitivity:(float)sensitivity;


// MARK: 切换清晰度
- (void)switchResolution:(NSInteger)resolutionId;
@end

NS_ASSUME_NONNULL_END
