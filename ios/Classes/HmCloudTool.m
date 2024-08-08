//
//  HmCloudTool.m
//  hm_cloud
//
//  Created by a水 on 2024/7/31.
//


#import "CloudPreViewController.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"


@interface HmCloudTool ()

@property (nonatomic, strong)  UIViewController *gameVC;
@property (nonatomic, strong)  CloudPreViewController *vc;


@end

@implementation HmCloudTool

+ (instancetype)share {
    static HmCloudTool *cloudTool;
    static dispatch_once_t token;

    dispatch_once(&token, ^{
        cloudTool = [[self alloc] init];
        cloudTool.isVibration = YES;
        cloudTool.touchMode = HMCloudCoreTouchModeMouse;
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
    self.vc = nil;
    self.gameVC = nil;
    [[HMCloudPlayer sharedCloudPlayer] stop:10 withReason:HMCloudAppStopReasonNormal];
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

// MARK: 延迟信息
- (void)cloudPlayerDelayInfoCallBack:(HMDelayInfoModel *)delayModel {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.vc) {
            [self.vc refreshfps:delayModel.frameRateEglRender ms:delayModel.pingpongCostTime rate:(delayModel.bitRate / 8) packetLoss:delayModel.packetLostPercent];
        }
    });
}

// MARK: 发送指令消息到海马
- (void)sendCustomKey:(NSArray<NSDictionary *> *)dictList {
    NSLog(@"sendCustomKey = %@", dictList);

    NSArray *opList = [dictList mapUsingBlock:^id _Nullable (NSDictionary *_Nonnull obj, NSUInteger idx) {
        return [HMOneInputOPData mj_objectWithKeyValues:obj];
    }];

    HMInputOpData *data = [[HMInputOpData alloc] init];

    data.opListArray = opList.mutableCopy;

    [[HMCloudPlayer sharedCloudPlayer] sendCustomKeycode:data];
}

// MARK: 更新鼠标模式
- (void)updateTouchMode:(HMCloudCoreTouchMode)touchMode {
    self.touchMode = touchMode;
    [[HMCloudPlayer sharedCloudPlayer] cloudSetTouchModel:self.touchMode];
}

// MARK: 获取鼠标灵敏度
- (float)sensitivity {
    return [[HMCloudPlayer sharedCloudPlayer] getMouseSensitivity];
}

- (NSString *)cloudId {
    return [HMCloudPlayer sharedCloudPlayer].cloudId;
}

// MARK: 更新鼠标灵敏度
- (void)updateMouseSensitivity:(float)sensitivity {
    [[HMCloudPlayer sharedCloudPlayer] setMouseSensitivity:sensitivity];
}

// MARK: ---------------解析海马代理

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
        [[HMCloudPlayer sharedCloudPlayer] cloudSetTouchModel:self.touchMode];

        if (!self.vc) {
            self.vc = [[CloudPreViewController alloc] initWithNibName:@"CloudPreViewController" bundle:k_SanABundle];
            self.vc.gameVC = self.gameVC;

            __weak typeof(self) weakSelf = self;

            self.vc.didDismiss = ^{
                typeof(self) strongSelf = weakSelf;
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
        NSString *reason = [info objectForKey:@"stop_reason"];

        if ([reason isEqualToString:@"no_operation"]) {
            // 长时间误操作 弹框
        } else {
            // 错误弹框
        }
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
