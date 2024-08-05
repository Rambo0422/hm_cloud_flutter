//
//  HmCloudTool.m
//  hm_cloud
//
//  Created by a水 on 2024/7/31.
//

#import <MJExtension/MJExtension.h>
#import "CloudPreViewController.h"
#import "HmCloudTool.h"
#import "UIViewController+TopVc.h"

#define SanA_Bundle [NSBundle bundleWithPath:[[NSBundle bundleForClass:self.class] pathForResource:@"SanA_Game" ofType:@"bundle"]]

@interface HmCloudTool ()

@property (nonatomic, strong)  UIViewController *gameVC;
@property (nonatomic, strong)  CloudPreViewController *vc;


@end

@implementation HmCloudTool

+ (instancetype)share {
    static id cloudTool;
    static dispatch_once_t token;

    dispatch_once(&token, ^{
        cloudTool = [[self alloc] init];
    });

    return cloudTool;
}

- (void)configWithParams:(NSDictionary *)params {
    [self mj_setKeyValues:params];
}

- (void)registWithDelegate:(id<HmCloudToolDelegate>)delegate {
    self.delegate = delegate;
    NSLog(@" =============== SDK_VERSION : %@", CLOUDGAME_SDK_VERSION);



    [[HMCloudPlayer sharedCloudPlayer] setDelegate:self];
    [[HMCloudPlayer sharedCloudPlayer] registCloudPlayer:self.accessKeyId channelId:self.channelName options:nil];
}

- (void)stop {
    [[HMCloudPlayer sharedCloudPlayer] stop];
}

- (void)restart {
    if (self.vc) {
        [self.delegate sendToFlutter:ActionClosePage
                              params:nil];
        [[UIViewController topViewController] presentViewController:self.vc animated:NO completion:nil];
    }
}

#pragma mark - CloudPlayer Delegate Function
- (void)cloudPlayerSceneChangedCallback:(NSDictionary *)dict {
    if (!dict || ![dict isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSLog(@"%s : %@", __FUNCTION__, dict);

    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *scene = [dict objectForKey:@"sceneId"];
        NSDictionary *extraInfo = [dict objectForKey:@"extraInfo"];

        if ([scene isEqualToString:@"init"]) {
            [self processSceneInitInfo:extraInfo];
        } else if ([scene isEqualToString:@"data"]) {
            [self processDataInfo:extraInfo];
        } else if ([scene isEqualToString:@"prepare"]) {
            [self processPrepareInfo:extraInfo];
        } else if ([scene isEqualToString:@"playerState"]) {
            [self processPlayerStateInfo:extraInfo];
        } else if ([scene isEqualToString:@"queue"]) {
            [self processQueueInfo:extraInfo];
        } else if ([scene isEqualToString:@"playingtime"]) {
            [self processPlayingTimeInfo:extraInfo];
        } else if ([scene isEqualToString:@"resolution"]) {
            [self processResolutionInfo:extraInfo];
        } else if ([scene isEqualToString:@"stastic"]) {
            [self processStasticInfo:extraInfo];
        } else if ([scene isEqualToString:@"maintance"]) {
            [self processMaintanceInfo:extraInfo];
        }
    });
}

- (void)processSceneInitInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"success"]) {
        [self initGameVC];
    } else if ([state isEqualToString:@"failed"]) {
    }
}

- (void)processDataInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *type = [info objectForKey:@"type"];

    if ([type isEqualToString:@"resolutions"]) {
        //设置清晰度切换按钮菜单
        NSArray *resolutions = [info objectForKey:@"data"];
    }

    if ([type isEqualToString:@"message"]) {
        //收到实例发送到客户端的消息
    }
}

- (void)processPrepareInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"success"]) {
        [[HMCloudPlayer sharedCloudPlayer] play];
    } else if ([state isEqualToString:@"failed"]) {
    }
}

- (void)processPlayerStateInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"prepared"]) {
    }

    if ([state isEqualToString:@"videoVisible"]) {
        [[HMCloudPlayer sharedCloudPlayer] cloudSetTouchModel:HMCloudCoreTouchModeScreen];

        if (!self.vc) {
            self.vc = [[CloudPreViewController alloc] initWithNibName:@"CloudPreViewController" bundle:SanA_Bundle];
            self.vc.gameVC = self.gameVC;

            __weak typeof(self) weakSelf = self;

            self.vc.didDismiss = ^{
                typeof(self) strongSelf = weakSelf;
                strongSelf.vc = nil;
                [strongSelf stop];
                
                if (strongSelf.delegate) {
                    [strongSelf.delegate sendToFlutter:ActionExitGame params:@{ @"action": @1 }];
                }
            };

            self.vc.pushFlutter = ^{
                typeof(self) strongSelf = weakSelf;
                [strongSelf.delegate sendToFlutter:ActionOpenPage
                                            params:@{ @"arguments": @{ @"type": @"rechargeTime",
                                                                       @"from": @"native" },
                                                      @"route": @"/rechargeCenter" }];
            };

            [[UIViewController topViewController] presentViewController:self.vc animated:YES completion:nil];
        }
    }

    if ([state isEqualToString:@"stopped"]) {
//        NSString *reason = [info objectForKey:@"stop_reason"];

//            if ([reason isEqualToString:@"no_operation"] || [reason isEqualToString:@"url_timeout"]) {
//                if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)]) {
//                    [_delegate cloudPlayerStateChanged:PlayerStateStopCanRetry];
//                }
//            } else {
//                if (_delegate && [_delegate respondsToSelector:@selector(cloudPlayerStateChanged:)]) {
//                    [_delegate cloudPlayerStateChanged:PlayerStateStop];
//                }
//            }
    }

    if ([state isEqualToString:@"playFailed"]) {
    }

    if ([state isEqualToString:@"timeout"]) {
    }

    if ([state isEqualToString:@"refreshSToken"]) {
    }
}

- (void)processQueueInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"confrim"]) {
        [[HMCloudPlayer sharedCloudPlayer] confirmQueue];
    }

    if ([state isEqualToString:@"update"]) {
        [self.delegate sendToFlutter:ActionQueueInfo params:@{ @"queueTime": info[@"second"] }];
    }

    if ([state isEqualToString:@"entering"]) {
    }
}

- (void)processPlayingTimeInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"prompt"]) {
    }

    if ([state isEqualToString:@"update"]) {
    }

    if ([state isEqualToString:@"totaltime"]) {
    }
}

- (void)processResolutionInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *type = [info objectForKey:@"type"];

    if ([type isEqualToString:@"notify"]) {
    }

    if ([type isEqualToString:@"crst"]) {
    }

    if ([type isEqualToString:@"cred"]) {
    }

    if ([type isEqualToString:@"crtp"]) {
    }
}

- (void)processStasticInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *type = [info objectForKey:@"type"];

    if ([type isEqualToString:@"bandwidth"]) {
    }

    if ([type isEqualToString:@"frames"]) {
    }

    if ([type isEqualToString:@"decode"]) {
    }
}

- (void)processMaintanceInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"progress"];

    if ([state isEqualToString:@"soon"]) {
    }

    if ([state isEqualToString:@"start"]) {
    }

    if ([state isEqualToString:@"inprogress"]) {
    }

    if ([state isEqualToString:@"done"]) {
    }
}

// MARK: 初始化gameVC
- (void)initGameVC {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSDictionary *dict = @{
                @"uid": self.userId,
                @"gameId": self.gameId,
                @"type": [self.priority intValue] > 46 ? @2 : @1,
        };


        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];

        NSString *jsonStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];


        NSDictionary *gameOptions = @{
                CloudGameOptionKeyId: self.gamePkName,
                CloudGameOptionKeyOrientation: @(0),
                CloudGameOptionKeyUserId: self.userId,
                CloudGameOptionKeyUserToken: self.userToken,
                CloudGameOptionKeyConfigInfo: @"config",
                CloudGameOptionKeyCToken: self.cToken,
                CloudGameOptionKeyPlayingTime: self.playTime,
                CloudGameOptionKeyExtraId: @"",
                CloudGameOptionKeyArchive: @(0),
                CloudGameOptionKeyProtoData: jsonStr,
                CloudGameOptionKeyAppChannel: self.channelName,
                CloudGameOptionKeyStreamType: @(CloudCoreStreamingTypeRTC),
                CloudGameOptionKeyPriority: self.priority
        };

        self.gameVC = [[HMCloudPlayer sharedCloudPlayer] prepare:gameOptions];
    });
}

@end
