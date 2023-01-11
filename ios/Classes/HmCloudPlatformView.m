//
//  HmCloudPlatformView.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/3.
//

#import "HmCloudPlatformView.h"
#import "HmCloudView.h"
#import "CloudPlayerWarpper.h"
#import <CommonCrypto/CommonDigest.h>
#import <CommonCrypto/CommonCryptor.h>
#import "CloudPreViewController.h"


@interface HmCloudPlatformView ()<CloudPlayerWarpperDelegate>


@property (nonatomic, strong)  FlutterMethodChannel  *channel;
@property (nonatomic, strong)  UIViewController        *gameVC;

@end

@implementation HmCloudPlatformView
{
    CGRect _frame;
    int64_t _viewId;
    id _args;
    HmCloudView *_v;
}

- (id)initWithFrame:(CGRect)frame
             viewId:(int64_t)viewId
               args:(id)args
           messager:(NSObject<FlutterBinaryMessenger>*)messenger{
    
    if (self = [super init]) {
        
        _frame = frame;
        _viewId = viewId;
        _args = args;
        
        /*
        if ([args isKindOfClass:[NSDictionary class]]) {
            
            NSDictionary * params = (NSDictionary *)args;
            
            // accessKey
            if (params[@"accessKey"]) {
                [CloudPlayerWarpper sharedWrapper].accessKey = params[@"accessKey"];
            }
            
            // accessKeyId
            if (params[@"accessKeyId"]) {
                [CloudPlayerWarpper sharedWrapper].accessKeyId = params[@"accessKeyId"];
            }
            
            // userId
            if (params[@"userId"]) {
                [CloudPlayerWarpper sharedWrapper].userId = params[@"userId"];
            }
            
            if (params[@"gameId"]) {
                [CloudPlayerWarpper sharedWrapper].gameId = params[@"gameId"];
            }
            
            // channelId
            if (params[@"channelId"]) {
                [CloudPlayerWarpper sharedWrapper].channelId = params[@"channelId"];
            }
            [CloudPlayerWarpper sharedWrapper].delegate = self;
            [[CloudPlayerWarpper sharedWrapper] regist];
            
        }
        */
         
        _channel = [FlutterMethodChannel methodChannelWithName:@"hm_cloud_controller" binaryMessenger:messenger codec:[FlutterStandardMethodCodec sharedInstance]];
        
    }
    __weak __typeof__(self) weakSelf = self;
    
    [_channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
        [weakSelf onMethodCall:call result:result];
    }];
    return self;
}

- (nonnull UIView *)view {
    
    if (_v) {
        return _v;
    } else {
        _v = [[HmCloudView alloc] initWithFrame:_frame];
        _v.multipleTouchEnabled = YES;
    }
    return _v;
    
}

#pragma mark -- Flutter 交互监听
-(void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result{
    //监听Fluter
    
    if ([[call method] isEqualToString:@"startCloudGame"]) {
        
        
        [self sendToFlutter:k_cloudInitBegan params:nil];
        
        if ([call.arguments isKindOfClass:[NSDictionary class]]) {
            
            NSDictionary * params = (NSDictionary *)call.arguments;
            
            // accessKey
            if (params[@"token"]) {
                [CloudPlayerWarpper sharedWrapper].cToken = params[@"token"];
            }
            
            // accessKeyId
            if (params[@"accessKeyId"]) {
                [CloudPlayerWarpper sharedWrapper].accessKeyId = params[@"accessKeyId"];
            }
            
            // expireTime
            if (params[@"expireTime"]) {
                [CloudPlayerWarpper sharedWrapper].expireTime = params[@"expireTime"];
            }
            
            // userId
            if (params[@"userId"]) {
                [CloudPlayerWarpper sharedWrapper].userId = params[@"userId"];
            }
            
            // gameId
            if (params[@"gameId"]) {
                [CloudPlayerWarpper sharedWrapper].gameId = params[@"gameId"];
            }
            
            // channelId
            if (params[@"channelId"]) {
                [CloudPlayerWarpper sharedWrapper].channelId = params[@"channelId"];
            }
            
            // userToken
            if (params[@"userToken"]) {
                [CloudPlayerWarpper sharedWrapper].userToken = params[@"userToken"];
            }
            
            // pushUrl
            if (params[@"pushUrl"]) {
                [CloudPlayerWarpper sharedWrapper].pushUrl = params[@"pushUrl"];
            }
            
            [CloudPlayerWarpper sharedWrapper].delegate = self;
            [[CloudPlayerWarpper sharedWrapper] regist];
            
        }
        
        
    }
    
    
    if ([[call method] isEqualToString:@"fullCloudGame"]) {
        
        if ([call.arguments isKindOfClass:[NSDictionary class]]) {
         
            NSDictionary * arguments = (NSDictionary *)call.arguments;
            NSNumber * isFull = arguments[@"isFull"];
            if (isFull) {
                [_v.subviews.firstObject removeFromSuperview];
                
                CloudPreViewController * vc = [[CloudPreViewController alloc] initWithNibName:@"CloudPreViewController" bundle:k_DaShenBundle];
                
                vc.modalPresentationStyle = UIModalPresentationFullScreen;
                vc.gameVC = self.gameVC;
                vc.channelAction = ^(NSString * _Nonnull methodName, bool value) {
                    [self sendToFlutter:methodName params:@{@"switch" : @(value)}];
                };
                
                vc.didDismiss = ^{
                    
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    
                        self.gameVC.view.frame = self->_v.bounds;
                        [self->_v insertSubview:self.gameVC.view atIndex:0];
                                                                    
                    });
                };
                
                [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:vc animated:YES completion:^{
                    [self sendToFlutter:k_startSuccess params:nil];
                }];
            }
        }
    }
    
    
    if ([call.method isEqualToString:@"stopGame"]) {
     
        [[CloudPlayerWarpper sharedWrapper] stop];
        [[CloudPlayerWarpper sharedWrapper] stopNetMonitor];
        
        [_v.subviews.firstObject removeFromSuperview];
        self.gameVC = nil;
    }
    
    
}

// 传值到flutter
- (void)sendToFlutter:(NSString *)actionName params:(id _Nullable)params {
    [self.channel invokeMethod:actionName arguments:params];
}

#pragma mark - CloudPlayerWrapper Delegate
- (void) cloudPlayerReigsted:(BOOL)success {
    
    if (success) {
     
        dispatch_async(dispatch_get_main_queue(), ^{
            
            NSDictionary *gameOptions = @{CloudGameOptionKeyId:[CloudPlayerWarpper sharedWrapper].gameId,
                                          CloudGameOptionKeyOrientation:@(0),
                                          CloudGameOptionKeyUserId:[CloudPlayerWarpper sharedWrapper].userId,
                                          CloudGameOptionKeyUserToken:[CloudPlayerWarpper sharedWrapper].userToken,
                                          CloudGameOptionKeyConfigInfo:@"config",
                                          CloudGameOptionKeyCToken:[CloudPlayerWarpper sharedWrapper].cToken,
                                          CloudGameOptionKeyPriority:@(0),
                                          CloudGameOptionKeyPlayingTime:[CloudPlayerWarpper sharedWrapper].expireTime,
                                          CloudGameOptionKeyExtraId:@"",
                                          CloudGameOptionKeyArchive:@(0),
                                          CloudGameOptionKeyProtoData:@"",
                                          CloudGameOptionKeyAppChannel:[CloudPlayerWarpper sharedWrapper].channelId,
                                          CloudGameOptionKeyStreamType:@(CloudCoreStreamingTypeRTC),
            };
            
            
//            NSLog(@"ccctoken = %@, %@",[CloudPlayerWarpper sharedWrapper].cToken,[self generateCToken:[CloudPlayerWarpper sharedWrapper].gameId]);
            
            
            self.gameVC = [[CloudPlayerWarpper sharedWrapper] prepare:gameOptions];
            
        });
        
        
        
    }
    
}

- (void) cloudPlayerResolutionList:(NSArray<HMCloudPlayerResolution*> *)resolutions {
}

- (void) cloudPlayerRecvMessage:(NSString *)msg {
}

- (void) cloudPlayerPrepared:(BOOL)success {

    if (success) {
        
        [[CloudPlayerWarpper sharedWrapper] play];
        
    }
}

- (void) cloudPlayerStateChanged:(CloudPlayerState)state {
    switch (state) {
        case PlayerStateInstancePrepared: { //实例申请完成
            NSLog(@"%s Show Loading .....", __FUNCTION__);
        }
            break;

        case PlayerStateVideoVisible: { //视频第一帧到达
            NSLog(@"%s Remove Loading", __FUNCTION__);
            
            dispatch_async(dispatch_get_main_queue(), ^{
                
                if (!self.gameVC) {
                    
                    [self sendToFlutter:k_startFailed params:nil];
                    return;
                }
                
                CloudPreViewController * vc = [[CloudPreViewController alloc] initWithNibName:@"CloudPreViewController" bundle:k_DaShenBundle];
                
                vc.modalPresentationStyle = UIModalPresentationFullScreen;
                vc.gameVC = self.gameVC;
                vc.channelAction = ^(NSString * _Nonnull methodName, bool value) {
                    [self sendToFlutter:methodName params:@{@"switch" : @(value)}];
                };
                
                vc.didDismiss = ^{
                    
                    
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    
                        self.gameVC.view.frame = self->_v.bounds;
                        [self->_v insertSubview:self.gameVC.view atIndex:0];
                                                                    
                    });
                    
                };
                
                [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:vc animated:YES completion:^{
                    [self sendToFlutter:k_startSuccess params:nil];
                }];
                
                
                [[CloudPlayerWarpper sharedWrapper] startNetMonitor];
                

            });
            
            // 开启直播
            [[HMCloudPlayer sharedCloudPlayer] startLivingWithLivingId:[CloudPlayerWarpper sharedWrapper].userId pushStreamUrl:[CloudPlayerWarpper sharedWrapper].pushUrl Success:^(BOOL success) {

                [self sendToFlutter:k_videoVisble params:nil];

            } Fail:^(NSString *errorCode, NSString *errorMsg) {
            }];
            
//            [self addRotateButton];
//            [self addPlayStatusButton];
        }
            break;

        case PlayerStateStopCanRetry: { //可以“重新连接”的出错
            NSLog(@"%s Stoped.", __FUNCTION__);
//            [self stopGame];
        }
            break;

        case PlayerStateStop: { //游戏结束，不可以“重新连接”
            NSLog(@"%s Stopped.", __FUNCTION__);
//            [self stopGame];
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

- (void) cloudPlayerTimeStateChanged:(CloudPlayerTimeState)state {
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

- (void) cloudPlayerResolutionStateChange:(CloudPlayerResolutionState)state {
    NSLog(@"%s", __FUNCTION__);
}

- (void) cloudPlayerStasticInfoReport:(CloudPlayerStasticState)state {
    NSLog(@"%s", __FUNCTION__);
}

- (void) cloudPlayerMaintanceStateChanged:(CloudPlayerMaintanceState)state {
    NSLog(@"%s", __FUNCTION__);
}


@end
