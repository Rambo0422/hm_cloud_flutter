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
static NSString *MethodInit = @"initSDK";
static NSString *MethodExitQueue = @"exitQueue";
static NSString *MethodClosePage = @"closePage";
static NSString *MethodBuySuccess = @"buySuccess";
static NSString *MethodUpdatePlayInfo = @"updatePlayInfo";
static NSString *MethodGetUnReleaseGame = @"getUnReleaseGame";
static NSString *MethodGetArchiveProgress = @"getArchiveProgress";
static NSString *MethodReleaseGame = @"releaseGame";

/// MARK: action name
static NSString *ActionExitGame = @"exitGame";
static NSString *ActionQueueInfo = @"queueInfo";
static NSString *ActionOpenPage = @"openPage";
static NSString *ActionUpdateTime = @"updateTime";
static NSString *ActionFirstFrameArrival = @"firstFrameArrival";


typedef void (^DataBlock)(NSDictionary *dict);
typedef void (^BoolBlock)(BOOL isSucc);

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

/// 记录xbox按下抬起的按键
@property (nonatomic, strong) NSMutableSet<NSNumber *> *xboxKeyList;

@property (nonatomic, weak) id<HmCloudToolDelegate> delegate;

+ (instancetype)share;
- (void)configWithParams:(NSDictionary *)params;
- (void)registWithDelegate:(id<HmCloudToolDelegate>)delegate;

- (void)startGame;

// 是否是vip
- (BOOL)isVip;

- (void)stopWithBack;
- (void)onlyStop;

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


/// 更新游玩时间
/// - Parameters:
///   - playTime: 游玩时间
- (void)updatePlayInfo:(NSDictionary *)playInfo;


/**
   将鼠标模式切换成pc模式
   仅x86游戏使用
   @param model true pc模式  false 移动端模式
 */
- (BOOL)convertToPcMouseModel:(BOOL)model;


/// 获取未释放的实力
/// @param block 数据回调
- (void)getUnReleaseGame:(nullable DataBlock)block;

/// 存档进度查询
/// @param block 成功失败回调
- (void)getArchiveResult:(nullable BoolBlock)block;

/// 释放游戏实例
/// @param block 成功失败回调
- (void)releaseGame:(nullable BoolBlock)block withParams:(NSDictionary *)params;

@end

NS_ASSUME_NONNULL_END
