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


@property (nonatomic, assign) BOOL isInit;

@end

@implementation HmCloudTool

- (NSMutableSet<NSNumber *> *)xboxKeyList {
    if (!_xboxKeyList) {
        _xboxKeyList = [NSMutableSet set];
    }

    return _xboxKeyList;
}

+ (NSDictionary *)mj_replacedKeyFromPropertyName {
    return @{
        @"account": @"accountInfo.account",
        @"password": @"accountInfo.password",
        @"platform_game_id": @"accountInfo.platform_game_id",
        @"key": @"accountInfo.key",
        @"accountToken": @"accountInfo.token",
        @"accountGameid": @"accountInfo.gameid",
        @"accountUserid": @"accountInfo.userid",
        @"platform": @"accountInfo.platform",
    };
}

+ (instancetype)share {
    static HmCloudTool *cloudTool;

    static dispatch_once_t token;

    dispatch_once(&token, ^{
        cloudTool = [[self alloc] init];
        cloudTool.isVibration = YES;
        cloudTool.touchMode = HMCloudCoreTouchModeMouse;

        [SVProgressHUD setFont:[UIFont systemFontOfSize:12]];
        [SVProgressHUD setMinimumSize:CGSizeMake(130, 50)];
        [SVProgressHUD setBackgroundColor:kColor(0x202125)];
        [SVProgressHUD setImageViewSize:CGSizeMake(1, 1)];
        [SVProgressHUD setForegroundColor:[UIColor whiteColor]];
    });

    return cloudTool;
}

- (void)configWithParams:(NSDictionary *)params {
    // 还原自动上号数据
    [self mj_setKeyValues:@{ @"accountInfo": @{ @"account": @"",
                                                @"password": @"",
                                                @"platform_game_id": @"",
                                                @"key": @"",
                                                @"token": @"",
                                                @"gameid": @"",
                                                @"userid": @"",
                                                @"platform": @"", } }];

    [self mj_setKeyValues:params];
}

- (void)registWithDelegate:(id<HmCloudToolDelegate>)delegate {
    self.delegate = delegate;
    NSLog(@" =============== SDK_VERSION : %@", CLOUDGAME_SDK_VERSION);

    if ([HmCloudTool share].playTime.integerValue < 0) {
        [ErrorAlertView showAlertWithCid:[HMCloudPlayer sharedCloudPlayer].cloudId
                                     uid:self.userId
                               errorCode:[NSString stringWithFormat:@"-1(%ld)", [HmCloudTool share].playTime.integerValue]
                                   title:@"特殊错误，请联系客服!"
                                 content:nil
                        dissMissCallback:^{
            [self stopWithBack];
            [[UIViewController topViewController] dismissViewControllerAnimated:YES
                                                                     completion:^{
            }];
        }];
        return;
    }

//    [self pushPreView];
//    return;

    [[HMCloudPlayer sharedCloudPlayer] setDelegate:self];
    [[HMCloudPlayer sharedCloudPlayer] registCloudPlayer:self.accessKeyId channelId:self.channelName options:nil];
}

- (void)stopWithBack {
    [self onlyStop];

    if (self.delegate) {
        [self.delegate sendToFlutter:ActionExitGame params:@{ @"action": @1 }];
    }
}

- (void)onlyStop {
    if (self.vc) {
        [self.vc stopTimer];
    }

    self.vc = nil;
    self.gameVC = nil;
    self.isInit = NO;

    [[HMCloudPlayer sharedCloudPlayer] stop:10 withReason:HMCloudAppStopReasonNormal];
}

- (void)restart {
    if (self.vc) {
        [[UIViewController topViewController] presentViewController:self.vc animated:NO completion:nil];
    }
}

- (void)updatePlayInfo:(NSDictionary *)playInfo {
    [self configWithParams:playInfo];

    NSLog(@"%@   %@", self.playTime, self.peakTime);

    [[HMCloudPlayer sharedCloudPlayer] updateGameUID:self.userId
                                           userToken:self.userToken
                                              ctoken:self.cToken
                                         playingTime:self.playTime.intValue
                                                 tip:nil
                                           protoData:nil
                                             success:^(BOOL successed) {
    }
                                                fail:^(NSString *errorCode) {
    }];
}

- (BOOL)convertToPcMouseModel:(BOOL)model {
    return [[HMCloudPlayer sharedCloudPlayer] convertToPcMouseModel:model];
}

- (void)getUnReleaseGame:(DataBlock)block {
    [[HMCloudPlayer sharedCloudPlayer] getReservedInstance:@{ CloudGameOptionKeyUserId: self.userId,
                                                              CloudGameOptionKeyUserToken: self.userToken, CloudGameOptionKeyAccessKeyId: self.accessKeyId }
                                         ReservedIncetance:^(NSArray<HMCloudPlayerReservedSingleIncetance *> *list) {
        NSArray *data = [list mapUsingBlock:^id _Nullable (HMCloudPlayerReservedSingleIncetance *_Nonnull obj, NSUInteger idx) {
            return @{ @"cid": obj.cid ? : @"",
                      @"pkgName": obj.pkgName ? : @"",
                      @"gameName": obj.gameName ? : @"",
                      @"appChannel": obj.appChannel ? : @"" };
        }];

        block(@{ @"isSucc": @1, @"data": data });
    }];
}

- (void)getArchiveResult:(BoolBlock)block {
    [[HMCloudPlayer sharedCloudPlayer] gameArchiveQuery:self.userId
                                              userToken:self.userToken
                                                pkgName:self.gamePkName
                                             appChannel:self.channelName
                                            accessKeyId:self.accessKeyId
                                                success:^(BOOL finished) {
        block(finished);
    }
                                                   fail:^(NSString *errorCode) {
        block(NO);
    }];
}

- (void)releaseGame:(BoolBlock)block withParams:(nonnull NSDictionary *)params {
    [[HMCloudPlayer sharedCloudPlayer] gameReleaseInstanceWithCid:params[@"cid"]
                                                           ctoken:params[@"cToken"]
                                                           userId:params[@"userId"]
                                                        userToken:params[@"userToken"]
                                                          pkgName:params[@"gamePkName"]
                                                       appChannel:params[@"channelName"]
                                                      accessKeyId:params[@"accessKeyId"]
                                                          success:^(BOOL released) {
        block(released);
    }
                                                             fail:^(NSString *errorCode) {
        block(NO);
    }];
}

- (void)getPinCode {
    [[HMCloudPlayer sharedCloudPlayer] getAuthCode];
}

- (void)pushPreView {
    if (!self.vc) {
        self.vc = [[CloudPreViewController alloc] initWithNibName:@"CloudPreViewController" bundle:k_SanABundle];
        self.vc.gameVC = self.gameVC;


        @weakify(self);

        self.vc.didDismiss = ^{
            @strongify(self);

            [self stopWithBack];
        };

        self.vc.pushFlutter = ^(FlutterPageType pageType) {
            @strongify(self);

            if (pageType == Flutter_rechartTime) {
                [self.delegate sendToFlutter:ActionOpenPage
                                      params:@{ @"arguments": @{ @"type": @"rechargeTime",
                                                                 @"from": @"native" },
                                                @"route": @"/rechargeCenter" }];
            }

            if (pageType == Flutter_rechartVip) {
                [self.delegate sendToFlutter:ActionOpenPage
                                      params:@{ @"arguments": @{ @"type": @"rechargeVip",
                                                                 @"from": @"native" },
                                                @"route": @"/rechargeCenter" }];
            }
        };



        [[UIViewController topViewController] presentViewController:self.vc animated:YES completion:nil];

        if (self.isPartyGame) {
            [self getPinCode];
        }
    }
}

- (BOOL)isVip {
    if (self.vipExpiredTime.integerValue > self.realTime.integerValue) {
        return YES;
    } else {
        return NO;
    }
}

- (void)startLiving {
    if (self.liveRoomId.length) {
        [self stopLiving];
    } else {
        HMCloudPlayer *player = [HMCloudPlayer sharedCloudPlayer];
        ELivingCapabilityStatus liveStatus = [player getLivingCapabilityStatus];

        if (liveStatus == LivingSupported) {
            NSString *pushUrl = [NSString stringWithFormat:@"rtmp://push-cg.3ayx.net/live/%@", self.cloudId];


            [[HMCloudPlayer sharedCloudPlayer] startLivingWithLivingId:self.cloudId
                                                         pushStreamUrl:pushUrl
                                                               success:nil
                                                                  fail:nil];
        }
    }
}

- (void)stopLiving {
    if (self.liveRoomId) {
        [[RequestTool share] requestUrl:k_api_update_liveRoom
                             methodType:Request_POST
                                 params:@{ @"id": self.liveRoomId, @"hide": self.isLiving ? @2 : @1 }
                          faildCallBack:nil
                        successCallBack: ^(id _Nonnull obj) {
            self.isLiving = !self.isLiving;
        }];
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
        } else if ([scene isEqualToString:@"living"]) {
            [self processLivingInfo:extraInfo];
        } else if ([scene isEqualToString:@"control"]) {
            [self processControlInfo:extraInfo];
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

- (void)cloudPlayerKeyboardStatusChanged:(CloudPlayerKeyboardStatus)status {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.vc) {
            [self.vc refreshKeyboardStatus:status];
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

// MARK: 切换清晰度
- (void)switchResolution:(NSInteger)resolutionId {
    [[HMCloudPlayer sharedCloudPlayer] switchResolution:resolutionId];
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
        self.isInit = YES;
    } else if ([state isEqualToString:@"failed"]) {
        NSString *errorCode = [info objectForKey:@"errorCode"];

        [ErrorAlertView showAlertWithCid:[HMCloudPlayer sharedCloudPlayer].cloudId
                                     uid:self.userId
                               errorCode:errorCode
                                   title:@"初始化失败！"
                                 content:nil
                        dissMissCallback:^{
            [self stopWithBack];
            [[UIViewController topViewController] dismissViewControllerAnimated:YES
                                                                     completion:^{
            }];
        }];
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

        [self.delegate sendToFlutter:ActionFirstFrameArrival params:nil];

        if (!self.isVip && !self.liveRoomId.length) {
            [self startLiving];
        }

        [self pushPreView];
    }

    if ([state isEqualToString:@"stopped"]) {
        NSString *reason = [info objectForKey:@"stop_reason"];

        NSString *title = @"";

        if ([reason isEqualToString:@"no_operation"]) {
            // 长时间误操作 弹框
            title = @"长时间未操作，请重新连接游戏";
        } else if ([reason isEqualToString:@"time_limit"]) {
            title = @"游戏时长已结束";
        } else {
            // 错误弹框
            NSString *errorCode = [info objectForKey:@"errorCode"];

            if (errorCode) {
                reason = [reason stringByAppendingFormat:@"--%@", errorCode];
            }

            title = @"哎呀，出现故障了！";
        }

        [ErrorAlertView showAlertWithCid:[HMCloudPlayer sharedCloudPlayer].cloudId
                                     uid:self.userId
                               errorCode:reason
                                   title:title
                                 content:nil
                        dissMissCallback:^{
            [self stopWithBack];
            [[UIViewController topViewController] dismissViewControllerAnimated:YES
                                                                     completion:^{
            }];
        }];
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

- (void)processLivingInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"startSuccess"]) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [[RequestTool share] requestUrl:k_api_create_liveRoom
                                 methodType:Request_POST
                                     params:@{ @"cid": [HmCloudTool share].cloudId, @"hide": @1 }
                              faildCallBack:^{
            }
                            successCallBack:^(id _Nonnull obj) {
                self.isLiving = YES;
                [SVProgressHUD showImage:[UIImage imageNamed:@""]
                                  status:@"云游互动开启成功~"];
                NSString *roomId = obj[@"room_id"];

                if (roomId) {
                    self.liveRoomId = roomId;
                }
            }];
        });
    }

    if ([state isEqualToString:@"startFailed"]) {
        [SVProgressHUD showImage:[UIImage imageNamed:@""] status:@"云游互动开启失败!"];
    }

    if ([state isEqualToString:@"stopSuccess"]) {
    }

    if ([state isEqualToString:@"stopFailed"]) {
    }
}

- (void)processControlInfo:(NSDictionary *)info {
    if (!info || ![info isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSString *state = [info objectForKey:@"state"];

    if ([state isEqualToString:@"authcodeSuccess"]) {
        [self.delegate sendToFlutter:ActionPinCodeResult
                              params:@{ @"cid": [NSString stringWithFormat:@"%@", info[@"masterCid"]],
                                        @"pinCode": [NSString stringWithFormat:@"%@", info[@"authcode"]] }];
    }
}

// MARK: 初始化gameVC
- (void)startGame {
    if (self.gameVC) {
        [[HMCloudPlayer sharedCloudPlayer] stop:10 withReason:HMCloudAppStopReasonNormal];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        NSDictionary *dict = @{
                @"uid": self.userId,
                @"gameId": self.gameId,
                @"type": [self.priority intValue] > 46 ? @2 : @1,
        };


        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];

        NSString *jsonStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];


        NSData *data = [jsonStr dataUsingEncoding:NSUTF8StringEncoding];
        NSString *base64String = [data base64EncodedStringWithOptions:0];


        NSMutableDictionary *gameOptions = @{
                CloudGameOptionKeyId: self.gamePkName,
                CloudGameOptionKeyOrientation: @(0),
                CloudGameOptionKeyUserId: self.userId,
                CloudGameOptionKeyUserToken: self.userToken,
                CloudGameOptionKeyConfigInfo: @"config",
                CloudGameOptionKeyCToken: self.cToken,
                CloudGameOptionKeyPlayingTime: self.playTime,
                CloudGameOptionKeyExtraId: @"",
                CloudGameOptionKeyArchive: @(0),
                CloudGameOptionKeyProtoData: base64String,
                CloudGameOptionKeyAppChannel: self.channelName,
                CloudGameOptionKeyStreamType: @(CloudCoreStreamingTypeRTC),
                CloudGameOptionKeyPriority: self.priority,
            }.mutableCopy;

        if (self.account.length || self.password.length || self.key.length || self.accountToken.length || self.accountGameid.length || self.accountUserid.length) {
            HMIntentExtraData *extraData = [[HMIntentExtraData alloc] init];
            extraData.stringExtra = [self stringExtraDataDict];

            [gameOptions setObject:extraData forKey:CloudGameOptionKeyIntentExtraData];
            [gameOptions setObject:@(CloudPlayerComponentTypeActivity) forKey:CloudGameOptionKeyComponentType];
        }

        self.gameVC = [[HMCloudPlayer sharedCloudPlayer] prepare:gameOptions];
    });
}

- (NSDictionary *)stringExtraDataDict {
    NSString *value = @"";

    if (self.account.length && self.password.length) {
        value = [NSString stringWithFormat:@"--platform=%@ --userid=%@ --gameid=%@ --account=%@ --password=%@ --platform_game_id=%@", self.platform, self.accountUserid, self.accountGameid, self.account, self.password, self.platform_game_id];
    } else if (self.key.length && self.accountToken.length) {
        value = [NSString stringWithFormat:@"--platform=%@ --userid=%@ --gameid=%@ --token=%@ --key=%@ --platform_game_id=%@ --mode=1", self.platform, self.accountUserid, self.accountGameid, self.accountToken, self.key, self.platform_game_id];
    } else {
        value = [NSString stringWithFormat:@"--platform=%@ --userid=%@ --gameid=%@ --platform_game_id=%@", self.platform, self.accountUserid, self.accountGameid, self.platform_game_id];
    }

    return @{
        @"StartAppParams": value
    };
}

@end
