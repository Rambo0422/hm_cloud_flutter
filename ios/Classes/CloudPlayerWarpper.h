//
//  CloudPlayerWarpper.h
//  YWCloudPlayer
//
//  Created by Apple on 2018/7/10.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <HMCloudPlayerCore/HMCloudPlayer.h>


#define DEMO_GAME_TIME          2000
#define DEMO_SAMPLE_INTERVAL    5


@class HMCloudPlayerResolution;
@protocol CloudPlayerWarpperDelegate;

@interface CloudPlayerWarpper : NSObject

+ (instancetype)sharedWrapper;

@property (nonatomic, assign) id<CloudPlayerWarpperDelegate> delegate;

@property (nonatomic, strong)   NSString *accessKeyId;
@property (nonatomic, strong)   NSString *userId;
@property (nonatomic, strong)   NSString *userToken;
@property (nonatomic, strong)   NSString *channelName;
@property (nonatomic, strong)   NSString *gameId;
@property (nonatomic, strong)   NSString *gamePkName;
@property (nonatomic, strong)   NSString *cToken;
@property (nonatomic, strong)   NSNumber *playTime;
@property (nonatomic, strong)   NSNumber *priority;


//- (NSString *)getcToken;

- (void)regist;
- (UIViewController *)prepare:(NSDictionary *)options;
- (void)setBackgroundImage:(UIImage *)bgImage;

- (void)updateGame;
- (void)play;
- (void)queueConfirm;
- (void)pause;
- (void)resume:(NSInteger)playingTime;
- (void)stop;
- (void)stopAndDismiss:(BOOL)animated;

- (void)swithcResolutin:(NSInteger)resolutionId;

- (void)sendMessage:(NSString *)msg;
- (void)sendCustomKey:(HMInputOpData *)data;
- (void)setMouseMode:(HMCloudCoreTouchMode)mouseMode;
- (void)setMouseSensitivity:(float)sensitivity;

- (void)startNetMonitor;
- (void)stopNetMonitor;

@end

typedef NS_ENUM(NSInteger, CloudPlayerState) {
    PlayerStateInstancePrepared,    //实例申请完成
    PlayerStateVideoVisible,       //视频第一帧到达
    PlayerStateStopCanRetry,       //可以“重新连接”的出错
    PlayerStateStop,               //游戏结束，不可以“重新连接”
    PlayerStateFailed,             //游戏失败
    PlayerStateTimeout,            //游戏时长到，且不结束游戏
    PlayerStateSToken              //刷新SToken
};

typedef NS_ENUM(NSInteger, CloudPlayerQueueState) {
    PlayerQueueStateConfirm,       //显示用户是否确认排队Dialog
    PlayerQueueStateUpdate,        //排队进度更新
    PlayerQueueStateEntering       //即将进入游戏
};

typedef NS_ENUM(NSInteger, CloudPlayerTimeState) {
    PlayerTimeStateNotify,         //游戏时长提示
    PlayerTimeStateUpdate,         //游戏时长更新通知
    PlayerTimeStateTotalTime       //本次游戏总时长提示
};

typedef NS_ENUM(NSInteger, CloudPlayerResolutionState) {
    PlayerResolutionStateNotify,   //当前播流清晰度通知
    PlayerResolutionStateSwitchStart,   //开始切换清晰度
    PlayerResolutionStateSwitchEnd,     //切换清晰度完成
    PlayerResolutionStateTip       //卡顿提示，建议用户切换网络
};

typedef NS_ENUM(NSInteger, CloudPlayerStasticState) {
    PlayerStasticStateBandwidth,   //统计时段内，带宽使用及最大N帧数组
    PlayerStasticStateFPS,         //统计时段内，视频总帧数
    PlayerStasticStateDecodeTime   //统计时段内，平均解码耗时，单位ms
};

typedef NS_ENUM(NSInteger, CloudPlayerMaintanceState) {
    PlayerMaintanceStateSoon,      //即将维护提示，游戏中用户会收到
    PlayerMaintanceStateStarted,   //维护开始，禁止进入游戏
    PlayerMaintanceStateInProgress,     //维护中，禁止进入游戏
    PlayerMaintanceStateDone       //维护完成
};

@protocol CloudPlayerWarpperDelegate <NSObject>
@optional

- (void)cloudPlayerReigsted:(BOOL)success;
- (void)cloudPlayerResolutionList:(NSArray<HMCloudPlayerResolution *> *)resolutions;
- (void)cloudPlayerRecvMessage:(NSString *)msg;

- (void)cloudPlayerPrepared:(BOOL)success;

- (void)cloudPlayerStateChanged:(CloudPlayerState)state;
- (void)cloudPlayerQueueStateChanged:(CloudPlayerQueueState)state;
- (void)cloudPlayerTimeStateChanged:(CloudPlayerTimeState)state;
- (void)cloudPlayerResolutionStateChange:(CloudPlayerResolutionState)state;
- (void)cloudPlayerStasticInfoReport:(CloudPlayerStasticState)state;
- (void)cloudPlayerMaintanceStateChanged:(CloudPlayerMaintanceState)state;
- (void)cloudPlayerDelayInfoCallBack:(HMDelayInfoModel *)delayModel;

- (void)cloudPlayerStop:(NSDictionary *)info;

@end

/********************************************** Main Loop Description *******************************
 1. Call [[CloudPlayerWarpper wrapped] regist] in AppDelegate didFinishLaunching function;
 1.1 Get ResolutionList by cloudPlayerResolutionList;
 1.2 Get Message from INSTANCE by cloudPlayerRecvMessage;


 2. Game Loop Description

 2.1 Call [[CloudPlayerWarpper wrapped] prepare] while Play button clicked, then present the retuned ViewController
 # Show Loading Hud
 # Set the backgroundImage

 2.2 Get Prepare Result by cloudPlayerPrepared
 # If success eq YES, then Call [[CloudPlayerWarpper wrapped] play]
 # If success eq NO, then show Error Dialog

 2.3 Implement cloudPlayerStateChanged Delegate function
 # PlayerStateInstancePrepared    --- Show Loading Hud
 # PlayerStateVideoVisible        --- Hide Loading Hud & Tips & Toast
 # PlayerStateStopCanRetry,       --- Show Dialog with RETRY button
 # PlayerStateStop                --- Show End Dialog
 # PlayerStateFailed              --- Show End Dialog
 # PlayerStateTimeout,            --- Show Playing Timeout Tip
 # PlayerStateSToken              --- SHow Loading Hud

 2.4 Stop Game
 # Call [[CloudPlayerWarpper wrapped] stop] if keep the Game View
 # Call [[CloudPlayerWarpper wrapped] stopAndDismiss] to Dismiss the Game ViewController


 3. Queue Process
 # PlayerQueueStateConfirm         --- Show QueueConfirm Dialog, User can select OK or Cancel.
 # PlayerQueueStateUpdate,         --- Show FullScreen View
 # PlayerQueueStateEntering        --- Show FullScreen View

 4. PlayingTime Process
 # PlayerTimeStateNotify           --- Show time notify toast, countdown or not.
 # PlayerTimeStateUpdate,          --- Show time updated notify toast
 # PlayerTimeStateTotalTime        --- Show TotalTime notify toast

 5. Resolution Process
 # PlayerResolutionStateNotify      --- Highlight SettingView Resolution Button If Needed
 # PlayerResolutionStateSwitchStart --- Show Resolution Changing Toast
 # PlayerResolutionStateSwitchEnd,  --- Show Resolution Changed Toast
 # PlayerResolutionStateTip         --- Show Suggest Tip

 6. StasticInfo Process
 # PlayerStasticStateBandwidth      --- Record Bandwidth data
 # PlayerStasticStateFPS,           --- Record FPS data
 # PlayerStasticStateDecodeTime     --- Record Decode Time date

 7. Maintance Process
 # PlayerMaintanceStateSoon         --- Show Maintance-Will-Start Tip
 # PlayerMaintanceStateStarted      --- Show End Dialog
 # PlayerMaintanceStateInProgress   --- Show End Dialog
 # PlayerMaintanceStateDone         --- Show Maintance-Done Tip

 ***************************************************************************************************/
