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
        
        _channel = [FlutterMethodChannel methodChannelWithName:@"hm_cloud_controller" binaryMessenger:messenger];
        
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
        _v.backgroundColor = [UIColor redColor];
    }
    return _v;
    
}

#pragma mark -- Flutter 交互监听
-(void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result{
    //监听Fluter
    
    if ([[call method] isEqualToString:@"startCloudGame"]) {
        
        
        NSDictionary *gameOptions = @{CloudGameOptionKeyId:[CloudPlayerWarpper sharedWrapper].gameId,
                                      CloudGameOptionKeyOrientation:@(0),
                                      CloudGameOptionKeyUserId:[CloudPlayerWarpper sharedWrapper].userId,
                                      CloudGameOptionKeyUserToken:[CloudPlayerWarpper sharedWrapper].userId,
                                      CloudGameOptionKeyConfigInfo:@"config",
                                      CloudGameOptionKeyCToken:[self generateCToken:[CloudPlayerWarpper sharedWrapper].gameId],
                                      CloudGameOptionKeyPriority:@(0),
                                      CloudGameOptionKeyPlayingTime:@(DEMO_GAME_TIME*1000),
                                      CloudGameOptionKeyExtraId:@"",
                                      CloudGameOptionKeyArchive:@(0),
                                      CloudGameOptionKeyProtoData:@"",
                                      CloudGameOptionKeyAppChannel:[CloudPlayerWarpper sharedWrapper].channelId,
                                      CloudGameOptionKeyStreamType:@(CloudCoreStreamingTypeRTC),
        };
        
        NSLog(@"startSDK params : %@", gameOptions);
        
        self.gameVC = [[CloudPlayerWarpper sharedWrapper] prepare:gameOptions];
        if (!self.gameVC) {
            NSLog(@"startSDK Failed .");
            return;
        }
        
        self.gameVC.view.frame = _v.bounds;
        [_v addSubview:self.gameVC.view];
        
        
        [[CloudPlayerWarpper sharedWrapper] startNetMonitor];
        
    }
    
}

#pragma mark - Demo Depended Function
- (NSString *) generateCToken:(NSString *)pkgName {
    NSData *keyData = [self hexToBytes:[CloudPlayerWarpper sharedWrapper].accessKey];
    Byte *keyArr = (Byte*)[keyData bytes];

    NSString *str = [NSString stringWithFormat:@"%@%@%@%@%@", [CloudPlayerWarpper sharedWrapper].userId, [CloudPlayerWarpper sharedWrapper].userId, pkgName, [CloudPlayerWarpper sharedWrapper].accessKeyId, [CloudPlayerWarpper sharedWrapper].channelId];
    NSData *strData = [str dataUsingEncoding:kCFStringEncodingUTF8];

    NSData *aesData = [self AES256EncryptWithKey:(void *)keyArr forData:strData ];//加密后的串
    return [self stringByHashingWithSHA1:aesData];
}

- (NSData *) hexToBytes:(NSString *)val {
    NSMutableData* data = [NSMutableData data];
    int idx;
    for (idx = 0; idx+2 <= val.length; idx+=2) {
        NSRange range = NSMakeRange(idx, 2);
        NSString * hexStr = [val substringWithRange:range];
        NSScanner * scanner = [NSScanner scannerWithString:hexStr];
        unsigned int intValue;
        [scanner scanHexInt:&intValue];
        [data appendBytes:&intValue length:1];
    }
    return data;
}

- (NSData *) AES256EncryptWithKey:(const void *)key forData:(NSData *)data {
    char keyPtr[kCCKeySizeAES256+1];
    bzero(keyPtr, sizeof(keyPtr));

    NSUInteger dataLength = [data length];
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer = malloc(bufferSize);
    size_t numBytesEncrypted = 0;
    CCCryptorStatus cryptStatus = CCCrypt(kCCEncrypt, kCCAlgorithmAES128,
                                          kCCOptionPKCS7Padding|kCCOptionECBMode,
                                          key, kCCBlockSizeAES128,
                                          NULL,
                                          [data bytes], dataLength,
                                          buffer, bufferSize,
                                          &numBytesEncrypted);
    if (cryptStatus == kCCSuccess) {
        return [NSData dataWithBytesNoCopy:buffer length:numBytesEncrypted];
    }
    free(buffer);

    return nil;
}

- (NSString *) stringByHashingWithSHA1:(NSData *)data {
    uint8_t digest[CC_SHA1_DIGEST_LENGTH];

    CC_SHA1(data.bytes, (unsigned int)data.length, digest);

    NSMutableString *output = [NSMutableString stringWithCapacity:CC_SHA1_DIGEST_LENGTH * 2];

    for(int i=0; i<CC_SHA1_DIGEST_LENGTH; i++) {
        [output appendFormat:@"%02x", digest[i]];
    }

    return output;
}

//调用Flutter
//- (void)flutterMethod{
//    [self.channel invokeMethod:@"clickAciton" arguments:@"我是参数"];
//}

#pragma mark - CloudPlayerWrapper Delegate
- (void) cloudPlayerReigsted:(BOOL)success {
    NSLog(@"%s : %@", __FUNCTION__, success?@"YES":@"NO");
}

- (void) cloudPlayerResolutionList:(NSArray<HMCloudPlayerResolution*> *)resolutions {
    NSLog(@"%s : %@", __FUNCTION__, resolutions);
}

- (void) cloudPlayerRecvMessage:(NSString *)msg {
    NSLog(@"%s : %@", __FUNCTION__, msg);
}

- (void) cloudPlayerPrepared:(BOOL)success {
    NSLog(@"%s : %@", __FUNCTION__, success?@"YES":@"NO");

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
