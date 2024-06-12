//
//  HmCloudPlatformView.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/3.
//

#import "HmCloudPlatformView.h"
#import "HmCloudView.h"
#import "CloudPlayerWarpper.h"
#import "CloudPreViewController.h"


@interface HmCloudPlatformView ()<CloudPlayerWarpperDelegate>


@property (nonatomic, strong)  FlutterMethodChannel     *channel;
@property (nonatomic, strong)  UIViewController         *gameVC;
@property (nonatomic, strong)  CloudPreViewController   *vc;

@end

@implementation HmCloudPlatformView
{
    CGRect _frame;
    int64_t _viewId;
    id _args;
//    HmCloudView *_v;
    UIView *_v;
}

- (id)initWithFrame:(CGRect)frame viewId:(int64_t)viewId args:(id)args messager:(NSObject<FlutterBinaryMessenger> *)messenger {
    if (self = [super init]) {
        _frame = frame;
        _viewId = viewId;
        _args = args;


        _channel = [FlutterMethodChannel methodChannelWithName:@"hm_cloud_controller" binaryMessenger:messenger codec:[FlutterStandardMethodCodec sharedInstance]];
    }

    __weak __typeof__(self) weakSelf = self;

    [_channel setMethodCallHandler:^(FlutterMethodCall *_Nonnull call, FlutterResult _Nonnull result) {
        [weakSelf onMethodCall:call
                        result:result];
    }];
    return self;
}

- (nonnull UIView *)view {
    if (_v) {
        return _v;
    } else {
        _v = [[UIView alloc] initWithFrame:_frame];
//        _v.multipleTouchEnabled = YES;
    }

    return _v;
}

#pragma mark -- Flutter 交互监听
- (void)onMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    //监听Fluter

    if ([[call method] isEqualToString:@"startCloudGame"]) {
        [self sendToFlutter:k_cloudInitBegan params:nil];

        if ([call.arguments isKindOfClass:[NSDictionary class]]) {
            NSDictionary *params = (NSDictionary *)call.arguments;
            [self startGame:params];
        }
    }

    if ([[call method] isEqualToString:@"updateGame"]) {
        if ([call.arguments isKindOfClass:[NSDictionary class]]) {
            NSDictionary *params = (NSDictionary *)call.arguments;
            [self updateGame:params];
        }
    }

    if ([call.method isEqualToString:@"stopGame"]) {
        [self stopGame];
    }

    if ([call.method isEqualToString:@"sendCustomKey"]) {
        id object = call.arguments;

        if ([object isKindOfClass:[NSDictionary class]]) {
            [self sendCustomKey:object];
        }
    }
    
    if ([call.method isEqualToString:@"setMouseMode"]) {
        id object = call.arguments;

        if ([object isKindOfClass:[NSNumber class]]) {
            [self setMouseMode:((NSNumber *)object).intValue];
        }
    }
    
    if ([call.method isEqualToString:@"setMouseSensitivity"]) {
        id object = call.arguments;

        if ([object isKindOfClass:[NSNumber class]]) {
            [self setMouseSensitivity:((NSNumber *)object).floatValue];
        }
    }

}

// MARK: 开始游戏
- (void)startGame:(NSDictionary *)params {
    // accessKeyId
    if (params[@"accessKeyId"]) {
        [CloudPlayerWarpper sharedWrapper].accessKeyId = params[@"accessKeyId"];
    }

    // accessKey
    if (params[@"cToken"]) {
        [CloudPlayerWarpper sharedWrapper].cToken = params[@"cToken"];
    }

    // playTime
    if (params[@"playTime"]) {
        [CloudPlayerWarpper sharedWrapper].playTime = params[@"playTime"];
    }

    // userId
    if (params[@"userId"]) {
        [CloudPlayerWarpper sharedWrapper].userId = params[@"userId"];
    }
    
    // gameId
    if (params[@"userId"]) {
        [CloudPlayerWarpper sharedWrapper].gameId = params[@"gameId"];
    }

    // gamePkName
    if (params[@"gamePkName"]) {
        [CloudPlayerWarpper sharedWrapper].gamePkName = params[@"gamePkName"];
    }

    // channelName
    if (params[@"channelName"]) {
        [CloudPlayerWarpper sharedWrapper].channelName = params[@"channelName"];
    }

    // userToken
    if (params[@"userToken"]) {
        [CloudPlayerWarpper sharedWrapper].userToken = params[@"userToken"];
    }

    // priority
    if (params[@"priority"]) {
        [CloudPlayerWarpper sharedWrapper].priority = params[@"priority"];
    }

    [CloudPlayerWarpper sharedWrapper].delegate = self;
    [[CloudPlayerWarpper sharedWrapper] regist];
}

// MARK: 停止游戏
- (void)stopGame {
    [[CloudPlayerWarpper sharedWrapper] stop];
    [[CloudPlayerWarpper sharedWrapper] stopNetMonitor];

    [_v.subviews.firstObject removeFromSuperview];
    self.gameVC = nil;
}

// MARK: 发送按键事件
- (void)sendCustomKey:(NSDictionary *)object {
    HMOneInputOPData *oneInputdata = [[HMOneInputOPData alloc] init];

    if (object[@"inputOp"]) {
        NSNumber *op = object[@"inputOp"];

        if ([op isKindOfClass:[NSNumber class]]) {
            oneInputdata.inputOp = [op intValue];
        }
    }

    if (object[@"inputState"]) {
        NSNumber *inputState = object[@"inputState"];

        if ([inputState isKindOfClass:[NSNumber class]]) {
            oneInputdata.inputState = [inputState intValue];
        }
    }

    if (object[@"value"]) {
        NSNumber *value = object[@"value"];

        if ([value isKindOfClass:[NSNumber class]]) {
            oneInputdata.value = [value intValue];
        }
    }

    if (![object[@"posCursor_x"] isKindOfClass:[NSNull class]] && ![object[@"posCursor_y"] isKindOfClass:[NSNull class]]) {
        NSNumber *posCursor_x = object[@"posCursor_x"];
        NSNumber *posCursor_y = object[@"posCursor_y"];

        HMCoordinatePos *pos = [[HMCoordinatePos alloc] init];
        pos.x = [posCursor_x integerValue];
        pos.y = [posCursor_y integerValue];

        oneInputdata.posCursor = pos;
    }

    if (![object[@"posMouse_x"] isKindOfClass:[NSNull class]] && ![object[@"posMouse_y"] isKindOfClass:[NSNull class]]) {
        NSNumber *posMouse_x = object[@"posMouse_x"];
        NSNumber *posMouse_y = object[@"posMouse_y"];

        HMCoordinatePos *pos = [[HMCoordinatePos alloc] init];
        pos.x = [posMouse_x integerValue];
        pos.y = [posMouse_y integerValue];

        oneInputdata.posMouse = pos;
    }

    HMInputOpData *inputData = [[HMInputOpData alloc] init];
    inputData.opListArray = @[oneInputdata].mutableCopy;

    [[CloudPlayerWarpper sharedWrapper] sendCustomKey:inputData];
}

// MARK: 更新游戏(主要更新游戏时长)
- (void)updateGame:(NSDictionary *)params {
    // accessKeyId
    if (params[@"accessKeyId"]) {
        [CloudPlayerWarpper sharedWrapper].accessKeyId = params[@"accessKeyId"];
    }

    // accessKey
    if (params[@"cToken"]) {
        [CloudPlayerWarpper sharedWrapper].cToken = params[@"cToken"];
    }

    // playTime
    if (params[@"playTime"]) {
        [CloudPlayerWarpper sharedWrapper].playTime = params[@"playTime"];
    }

    // userId
    if (params[@"userId"]) {
        [CloudPlayerWarpper sharedWrapper].userId = params[@"userId"];
    }

    // gameId
    if (params[@"userId"]) {
        [CloudPlayerWarpper sharedWrapper].gameId = params[@"gameId"];
    }
    
    // gamePkName
    if (params[@"gamePkName"]) {
        [CloudPlayerWarpper sharedWrapper].gamePkName = params[@"gamePkName"];
    }

    // channelName
    if (params[@"channelName"]) {
        [CloudPlayerWarpper sharedWrapper].channelName = params[@"channelName"];
    }

    // userToken
    if (params[@"userToken"]) {
        [CloudPlayerWarpper sharedWrapper].userToken = params[@"userToken"];
    }

    // priority
    if (params[@"priority"]) {
        [CloudPlayerWarpper sharedWrapper].priority = params[@"priority"];
    }

    [[CloudPlayerWarpper sharedWrapper] updateGame];
}
    
// MARK: 设置鼠标模式
/// 0:禁用鼠标
/// 1:鼠标点击
/// 2:触动点击
/// 3:触屏攻击
- (void)setMouseMode:(int)mouseMode {
    HMCloudCoreTouchMode mode = HMCloudCoreTouchModeNone;

    switch (mouseMode) {
        case 0:
            mode = HMCloudCoreTouchModeNone;
            break;

        case 1:
            mode = HMCloudCoreTouchModeMouse;
            break;

        case 2:
            mode = HMCloudCoreTouchModeScreen;
            break;

        case 3:
            mode = HMCloudCoreTouchModeFingerTouch;
            break;

        default:
            break;
    }

    [[CloudPlayerWarpper sharedWrapper] setMouseMode:mode];
}

// MARK: 设置鼠标灵敏度 0 ~ 1
- (void)setMouseSensitivity:(float)sensitivity {
    [[CloudPlayerWarpper sharedWrapper] setMouseSensitivity:sensitivity];
}

// 传值到flutter
- (void)sendToFlutter:(NSString *)actionName params:(id _Nullable)params {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.channel invokeMethod:actionName arguments:params];
    });
}

#pragma mark - CloudPlayerWrapper Delegate
- (void)cloudPlayerReigsted:(BOOL)success {
    if (success) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSDictionary *dict = @{
                    @"uid": [CloudPlayerWarpper sharedWrapper].userId,
                    @"gameId": [CloudPlayerWarpper sharedWrapper].gameId,
                    @"type": [[CloudPlayerWarpper sharedWrapper].priority intValue] > 46 ? @2 : @1,
            };



            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];

            NSString *jsonStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];


            NSDictionary *gameOptions = @{
                    CloudGameOptionKeyId: [CloudPlayerWarpper sharedWrapper].gamePkName,
                    CloudGameOptionKeyOrientation: @(0),
                    CloudGameOptionKeyUserId: [CloudPlayerWarpper sharedWrapper].userId,
                    CloudGameOptionKeyUserToken: [CloudPlayerWarpper sharedWrapper].userToken,
                    CloudGameOptionKeyConfigInfo: @"config",
                    CloudGameOptionKeyCToken: [CloudPlayerWarpper sharedWrapper].cToken,
                    CloudGameOptionKeyPlayingTime: [CloudPlayerWarpper sharedWrapper].playTime,
                    CloudGameOptionKeyExtraId: @"",
                    CloudGameOptionKeyArchive: @(0),
                    CloudGameOptionKeyProtoData: @"",
                    CloudGameOptionKeyAppChannel: [CloudPlayerWarpper sharedWrapper].channelName,
                    CloudGameOptionKeyStreamType: @(CloudCoreStreamingTypeRTC),
                    CloudGameOptionKeyPriority: [CloudPlayerWarpper sharedWrapper].priority
            };

            self.gameVC = [[CloudPlayerWarpper sharedWrapper] prepare:gameOptions];
        });
    }
}

- (void) cloudPlayerResolutionList:(NSArray<HMCloudPlayerResolution*> *)resolutions {
}

- (void) cloudPlayerRecvMessage:(NSString *)msg {
}

- (void)cloudPlayerPrepared:(BOOL)success {
    if (success) {
        [[CloudPlayerWarpper sharedWrapper] play];
    }
}

- (void)cloudPlayerStateChanged:(CloudPlayerState)state {
    switch (state) {
        case PlayerStateInstancePrepared: { //实例申请完成
            NSLog(@"%s Show Loading .....", __FUNCTION__);
        }
        break;

        case PlayerStateVideoVisible: { //视频第一帧到达
            

            dispatch_async(dispatch_get_main_queue(), ^{
                if (!self.gameVC) {
                    [self sendToFlutter:k_startFailed params:nil];
                    return;
                }

                self.gameVC.view.frame = self->_v.bounds;
                [self->_v insertSubview:self.gameVC.view atIndex:0];

                [self sendToFlutter:k_FirstFrameArrival
                             params:nil];

                [[CloudPlayerWarpper sharedWrapper] startNetMonitor];
            });

            // 开启直播


            //            [[HMCloudPlayer sharedCloudPlayer] startLivingWithLivingId:[CloudPlayerWarpper sharedWrapper].userId
            //                                                         pushStreamUrl:[CloudPlayerWarpper sharedWrapper].pushUrl
            //                                                               success:^(BOOL success) {
            //
            //                [self sendToFlutter:k_videoVisble params:nil];
            //            }
            //                                                                  fail:^(NSString *errorCode, NSString *errorMsg) {
            //            }];
        }
        break;

        case PlayerStateStopCanRetry: { //可以“重新连接”的出错
            case PlayerStateStop: { //游戏结束，不可以“重新连接”
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[CloudPlayerWarpper sharedWrapper] stop];
                    [[CloudPlayerWarpper sharedWrapper] stopNetMonitor];
                    
                });
            }
            break;

            case PlayerStateFailed: { //游戏失败
                NSLog(@"%s Failed.", __FUNCTION__);
                //            [self stopGame];
            }
            break;

            case PlayerStateTimeout: { //游戏时长到，且不结束游戏
                NSLog(@"%s Timeout.", __FUNCTION__);
                //            [self stopGame];
            }
            break;

            case PlayerStateSToken: { //刷新SToken
                NSLog(@"%s Show Loading .....", __FUNCTION__);
            }

            default:
                break;
        }
    }
}

- (void) cloudPlayerQueueStateChanged:(CloudPlayerQueueState)state {
    switch (state) {
        case PlayerQueueStateConfirm: { //显示用户是否确认排队Dialog
            NSLog(@"%s Show QueueConfrim Dialog", __FUNCTION__);
            [[CloudPlayerWarpper sharedWrapper] queueConfirm];
        }
        break;

        case PlayerQueueStateUpdate: { //排队进度更新
            NSLog(@"%s Show FullScreen QueueStatus View", __FUNCTION__);

            [self sendToFlutter:k_cloudQueueInfo params:nil];
        }
        break;

        case PlayerQueueStateEntering: { //即将进入游戏
            NSLog(@"%s Show FullScreen Entering View", __FUNCTION__);
        }
        break;

        default:
            break;
    }
}

- (void)cloudPlayerTimeStateChanged:(CloudPlayerTimeState)state {
    switch (state) {
        case PlayerTimeStateNotify: { //游戏时长提示
            NSLog(@"%s Show PlayingTime Notify Tip", __FUNCTION__);
        }
        break;

        case PlayerTimeStateUpdate: { //游戏时长更新通知
            NSLog(@"%s Show PlayingTime Updated Tip", __FUNCTION__);
        }
        break;

        case PlayerTimeStateTotalTime: { //本次游戏总时长提示
            NSLog(@"%s Show TotalPlayingTime Tip", __FUNCTION__);
        }
        break;

        default:
            break;
    }
}

- (void)cloudPlayerResolutionStateChange:(CloudPlayerResolutionState)state {
    NSLog(@"%s", __FUNCTION__);
}

- (void)cloudPlayerStasticInfoReport:(CloudPlayerStasticState)state {
    NSLog(@"%s", __FUNCTION__);
}

- (void)cloudPlayerMaintanceStateChanged:(CloudPlayerMaintanceState)state {
    NSLog(@"%s", __FUNCTION__);
}

- (void)cloudPlayerDelayInfoCallBack:(HMDelayInfoModel *)delayModel {
    [self sendToFlutter:k_DelayInfo
                 params:@{
         @"pingpongCostTime": @(delayModel.pingpongCostTime),
         @"packetLostPercent": @(delayModel.packetLostPercent),
    }];
}

- (void)cloudPlayerStop:(NSDictionary *)info {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *reason = [info objectForKey:@"stop_reason"];
        NSString *errorCode = [info objectForKey:@"errorCode"];
        // 如果cid为空，则是因为游戏并没有启动成功
        [[CloudPlayerWarpper sharedWrapper] stop];
        [[CloudPlayerWarpper sharedWrapper] stopNetMonitor];
        [self sendToFlutter:k_GameStop
                     params:@{
             @"cid": [HMCloudPlayer sharedCloudPlayer].cloudId ? : @"",
             @"errorCode": errorCode ?: reason,
        }];
    });
}

@end
