//
//  CloudPlayerWarpper.m
//  YWCloudPlayer
//
//  Created by Apple on 2018/7/10.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import "CloudPlayerWarpper.h"
#import <AVFoundation/AVFoundation.h>

@interface CloudPlayerWarpper () <HMCloudPlayerDelegate> {
    BOOL isInitialized;
}

@end

@implementation CloudPlayerWarpper

+ (instancetype) sharedWrapper {
    static id cloudPlayerWarpper;
    static dispatch_once_t cloudPlayerWarpperToken;
    dispatch_once(&cloudPlayerWarpperToken, ^{
        cloudPlayerWarpper = [[self alloc] init];
    });

    return cloudPlayerWarpper;
}

- (instancetype) init {
    if (self = [super init]) {
        isInitialized = NO;
    }

    return self;
}


- (void) regist {
//    if (isInitialized) return;

    NSLog(@" =============== SDK_VERSION : %@", CLOUDGAME_SDK_VERSION);

    [[HMCloudPlayer sharedCloudPlayer] setDelegate:self];
    [[HMCloudPlayer sharedCloudPlayer] registCloudPlayer:self.accessKeyId channelId:self.channelId options:nil];
}

- (void)updateGame{
    
    [[HMCloudPlayer sharedCloudPlayer] updateGameUID:self.userId userToken:self.userToken ctoken:self.cToken playingTime:[self.expireTime integerValue] tip:@"" protoData:nil success:^(BOOL successed) {
        
    } fail:^(NSString *errorCode) {
        
    }];
    
}

- (UIViewController *) prepare:(NSDictionary *)options {
    if (!isInitialized) return NULL;

    return [[HMCloudPlayer sharedCloudPlayer] prepare:options];
}

- (void) setBackgroundImage:(UIImage *)bgImage {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] setBackgroundImage:bgImage];
}

- (void) play {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] play];
}
- (void) queueConfirm {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] confirmQueue];
}

- (void) pause {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] pause];
}

- (void) resume:(NSInteger)playingTime {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] resume:playingTime];
}

- (void) stop {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] stop];
}

- (void) stopAndDismiss:(BOOL)animated {
    if (!isInitialized) return;

    if ([NSThread isMainThread])
        [[HMCloudPlayer sharedCloudPlayer] stopAndDismiss:NO];
    else {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[HMCloudPlayer sharedCloudPlayer] stopAndDismiss:animated];
        });
    }
}

- (void) swithcResolutin:(NSInteger)resolutionId {
    if (!isInitialized) return;

    [[HMCloudPlayer sharedCloudPlayer] switchResolution:resolutionId];
}

- (void) sendMessage:(NSString *)msg {
    if (!isInitialized) return;
    if (!msg || ![msg isKindOfClass:[NSString class]] || !msg.length) return;

    [[HMCloudPlayer sharedCloudPlayer] sendMessage:msg];
}

#pragma mark CloudPlayer Delegate
- (void) cloudPlayerSceneChangedCallback:(NSDictionary *)dict {
    if (!dict || ![dict isKindOfClass:[NSDictionary class]]) return;

    NSLog(@"%s : %@", __FUNCTION__, dict);

    @autoreleasepool {
        NSString *scene = [dict objectForKey:@"sceneId"];
        NSDictionary *extraInfo = [dict objectForKey:@"extraInfo"];

        do {
            if ([scene isEqualToString:@"init"]) {
                [self processSceneInitInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"data"]) {
                [self processDataInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"prepare"]) {
                [self processPrepareInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"playerState"]) {
                [self processPlayerStateInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"queue"]) {
                [self processQueueInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"playingtime"]) {
                [self processPlayingTimeInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"resolution"]) {
                [self processResolutionInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"stastic"]) {
                [self processStasticInfo:extraInfo];
                break;
            }

            if ([scene isEqualToString:@"maintance"]) {
                [self processMaintanceInfo:extraInfo];
                break;
            }
        } while (0);
    }
}

- (void) cloudPlayerTouchBegan {
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"touchBegan" object:nil];
    
}

#pragma mark - CloudPlayer Delegate Function
- (void) processSceneInitInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *state = [info objectForKey:@"state"];
//    NSString *errorCode = [info objectForKey:@"errorCode"];

    if ([state isEqualToString:@"success"]) {
        isInitialized = YES;

        if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerReigsted:)])
            [_delegate cloudPlayerReigsted:YES];
    } else if ([state isEqualToString:@"failed"]) {
        isInitialized = NO;
        
        if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerReigsted:)])
            [_delegate cloudPlayerReigsted:NO];
    }
}

- (void) processDataInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    do {
        NSString *type = [info objectForKey:@"type"];

        if ([type isEqualToString:@"resolutions"]) {
            //设置清晰度切换按钮菜单
            NSArray *resolutions = [info objectForKey:@"data"];
            if (resolutions && [resolutions isKindOfClass:[NSArray class]]) {
                if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerResolutionList:)])
                    [_delegate cloudPlayerResolutionList:[NSArray arrayWithArray:resolutions]];
            }
            break;
        }

        if ([type isEqualToString:@"message"]) {
            //收到实例发送到客户端的消息
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerRecvMessage:)])
                [_delegate cloudPlayerRecvMessage:[info objectForKey:@"data"]];
            break;
        }
    } while (0);
}

- (void) processPrepareInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"success"]) {
        if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerPrepared:)])
            [_delegate cloudPlayerPrepared:YES];
    } else if ([state isEqualToString:@"failed"]) {
        if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerPrepared:)])
            [_delegate cloudPlayerPrepared:NO];
    }
}

- (void) processPlayerStateInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *state = [info objectForKey:@"state"];

    do {
        if ([state isEqualToString:@"prepared"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                [_delegate cloudPlayerStateChanged:PlayerStateInstancePrepared];
            break;
        }

        if ([state isEqualToString:@"videoVisible"]) {
            [[HMCloudPlayer sharedCloudPlayer] cloudSetTouchModel:HMCloudCoreTouchModeMouse];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                [_delegate cloudPlayerStateChanged:PlayerStateVideoVisible];
            break;
        }

        if ([state isEqualToString:@"stopped"]) {
//            NSString *errorCode = [info objectForKey:@"errorCode"];
//            NSString *errorMsg = [info objectForKey:@"errorMsg"];
            NSString *reason = [info objectForKey:@"stop_reason"];

            if ([reason isEqualToString:@"no_operation"] || [reason isEqualToString:@"url_timeout"]) {
                if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                    [_delegate cloudPlayerStateChanged:PlayerStateStopCanRetry];
            } else {
                if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                    [_delegate cloudPlayerStateChanged:PlayerStateStop];
            }
            break;
        }

        if ([state isEqualToString:@"playFailed"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                [_delegate cloudPlayerStateChanged:PlayerStateFailed];
            break;
        }

        if ([state isEqualToString:@"timeout"]) {
//            NSString *tip = [info objectForKey:@"tip"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                [_delegate cloudPlayerStateChanged:PlayerStateTimeout];
            break;
        }

        if ([state isEqualToString:@"refreshSToken"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)])
                [_delegate cloudPlayerStateChanged:PlayerStateSToken];
            break;
        }
    } while (0);
}

- (void) processQueueInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *state = [info objectForKey:@"state"];
//    NSString *title = [info objectForKey:@"title"];

    do {
        if ([state isEqualToString:@"confrim"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerQueueStateChanged:)])
                [_delegate cloudPlayerQueueStateChanged:PlayerQueueStateConfirm];
            break;
        }

        if ([state isEqualToString:@"update"]) {
//            NSInteger index = [[info objectForKey:@"index"] integerValue];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerQueueStateChanged:)])
                [_delegate cloudPlayerQueueStateChanged:PlayerQueueStateUpdate];
            break;
        }

        if ([state isEqualToString:@"entering"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerQueueStateChanged:)])
                [_delegate cloudPlayerQueueStateChanged:PlayerQueueStateEntering];
            break;
        }
    } while (0);
}

- (void) processPlayingTimeInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *state = [info objectForKey:@"state"];

    do {
        if ([state isEqualToString:@"prompt"]) {
//            NSInteger seconds = [[info objectForKey:@"second"] integerValue];
//            NSString *title = [info objectForKey:@"title"];
//            BOOL countdown = [[info objectForKey:@"countdown"] boolValue];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerTimeStateChanged:)])
                [_delegate cloudPlayerTimeStateChanged:PlayerTimeStateNotify];
            break;
        }

        if ([state isEqualToString:@"update"]) {
//            NSString *title = [info objectForKey:@"title"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerTimeStateChanged:)])
                [_delegate cloudPlayerTimeStateChanged:PlayerTimeStateUpdate];
            break;
        }

        if ([state isEqualToString:@"totaltime"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerTimeStateChanged:)])
                [_delegate cloudPlayerTimeStateChanged:PlayerTimeStateTotalTime];
            break;
        }
    } while (0);
}

- (void) processResolutionInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *type = [info objectForKey:@"type"];

    do {
        if ([type isEqualToString:@"notify"]) {
//            NSInteger resolutionId = [[info objectForKey:@"cur_rate"] integerValue];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerResolutionStateChange:)])
                [_delegate cloudPlayerResolutionStateChange:PlayerResolutionStateNotify];
            break;
        }

        if ([type isEqualToString:@"crst"]) {
//            NSString *title = [info objectForKey:@"title"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerResolutionStateChange:)])
                [_delegate cloudPlayerResolutionStateChange:PlayerResolutionStateSwitchStart];
            break;
        }

        if ([type isEqualToString:@"cred"]) {
//            NSString *title = [info objectForKey:@"title"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerResolutionStateChange:)])
                [_delegate cloudPlayerResolutionStateChange:PlayerResolutionStateSwitchEnd];
            break;
        }

        if ([type isEqualToString:@"crtp"]) {
//            NSString *title = [info objectForKey:@"title"];
//            BOOL isMinimum = [[info objectForKey:@"minimum"] boolValue];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerResolutionStateChange:)])
                [_delegate cloudPlayerResolutionStateChange:PlayerResolutionStateTip];
            break;
        }
    } while (0);
}

- (void) processStasticInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *type = [info objectForKey:@"type"];

    do {
        if ([type isEqualToString:@"bandwidth"]) {
//            NSInteger value = [[info objectForKey:@"value"] integerValue];
//            NSArray *frames = [info objectForKey:@"frames"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStasticInfoReport:)])
                [_delegate cloudPlayerStasticInfoReport:PlayerStasticStateBandwidth];
            break;
        }

        if ([type isEqualToString:@"frames"]) {
//            NSInteger value = [[info objectForKey:@"value"] integerValue];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStasticInfoReport:)])
                [_delegate cloudPlayerStasticInfoReport:PlayerStasticStateFPS];
            break;
        }

        if ([type isEqualToString:@"decode"]) {
//            NSInteger value = [[info objectForKey:@"value"] integerValue];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStasticInfoReport:)])
                [_delegate cloudPlayerStasticInfoReport:PlayerStasticStateDecodeTime];
            break;
        }
    } while (0);
}

- (void) processMaintanceInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) return;

    NSString *state = [info objectForKey:@"progress"];

    do {
        if ([state isEqualToString:@"soon"]) {
//            NSString *title = [info objectForKey:@"title"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerMaintanceStateChanged:)])
                [_delegate cloudPlayerMaintanceStateChanged:PlayerMaintanceStateSoon];
            break;
        }

        if ([state isEqualToString:@"start"]) {
//            NSString *title = [info objectForKey:@"title"];
//            NSString *errorCode = [info objectForKey:@"errorCode"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerMaintanceStateChanged:)])
                [_delegate cloudPlayerMaintanceStateChanged:PlayerMaintanceStateStarted];
            break;
        }

        if ([state isEqualToString:@"inprogress"]) {
//            NSString *title = [info objectForKey:@"title"];
//            NSString *errorCode = [info objectForKey:@"errorCode"];
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerMaintanceStateChanged:)])
                [_delegate cloudPlayerMaintanceStateChanged:PlayerMaintanceStateInProgress];
            break;
        }

        if ([state isEqualToString:@"done"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerMaintanceStateChanged:)])
                [_delegate cloudPlayerMaintanceStateChanged:PlayerMaintanceStateDone];
        }
    } while (0);
}

- (void)cloudPlayerUsageAuthorization:(HMCloudPlayerUsageAuthorization)type success:(void (^)(BOOL authorization))success {
    if (type == HMCloudPlayerUsageAuthorizationMicrophone) {

        AVAuthorizationStatus microPhoneStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeAudio];
        //授权情况
        if (microPhoneStatus == AVAuthorizationStatusAuthorized) {
            //有授权
            if (success) {
                success(YES);
            }
        } else if (microPhoneStatus == AVAuthorizationStatusNotDetermined) {
            //无弹窗，主动弹窗
            [AVCaptureDevice requestAccessForMediaType:AVMediaTypeAudio completionHandler:^(BOOL granted) {
                if (granted) {
                    //有授权
                    if (success) {
                        success(YES);
                    }
                } else {
                    if (success) {
                        success(NO);
                    }
                }
            }];
        } else {
            //非第一次访问，拒绝状态
            if (success) {
                success(NO);
            }
        }

    } else if (type == HMCloudPlayerUsageAuthorizationCamera) {

        AVAuthorizationStatus cameraStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
        if (cameraStatus == AVAuthorizationStatusAuthorized) {
            //有授权
            if (success) {
                success(YES);
            }
        } else if (cameraStatus == AVAuthorizationStatusNotDetermined) {
            //从未访问 需要弹窗
            [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
                if (granted) {
                    //授权
                    if (success) {
                        success(YES);
                    }
                } else {
                    //相机未授权
                    if (success) {
                        success(NO);
                    }
                }
            }];
        } else {
            //非第一次访问，拒绝状态
            if (success) {
                success(NO);
            }
        }
    }
}

#pragma mark - 网络监控
- (void)startNetMonitor {
    __block CloudCorePlayerNetStatus curStatus = NetStatusUnknown;
    [[HMCloudPlayer sharedCloudPlayer] startNetMonitor:^(CloudCorePlayerNetStatus status) {
        switch (status) {
            case NetStatusNotReachable:
                    [self pause];
                break;
            case NetStatusReachableViaWiFi:
                if (curStatus == NetStatusNotReachable) {
                    [[HMCloudPlayer sharedCloudPlayer] resume:0];
                }
                break;
            case NetStatusReachableViaWWAN:
                if (curStatus == NetStatusNotReachable) {
                    [[HMCloudPlayer sharedCloudPlayer] resume:0];
                }
                break;
            default:
                break;
        }
        curStatus = status;
    }];
}

- (void)stopNetMonitor{
    [[HMCloudPlayer sharedCloudPlayer] stopNetMonitor];
}

@end
