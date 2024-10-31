#import "HmCloudPlugin.h"
#import "HmCloudTool.h"

@interface HmCloudPlugin ()<HmCloudToolDelegate>

@property (nonatomic, strong)  FlutterMethodChannel *channel;

@end

@implementation HmCloudPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
                                     methodChannelWithName:@"hm_cloud_controller"
                                           binaryMessenger:[registrar messenger]];

    HmCloudPlugin *instance = [[HmCloudPlugin alloc] initWithChannel:channel];

    [registrar addMethodCallDelegate:instance channel:instance.channel];
}

- (instancetype)initWithChannel:(FlutterMethodChannel *)channel
{
    self = [super init];

    if (self) {
        self.channel = channel;
    }

    return self;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([call.method isEqualToString:MethodInit]) {
        [[HmCloudTool share] configWithParams:call.arguments];

        [[HmCloudTool share] registWithDelegate:self];
    }

    if ([call.method isEqualToString:MethodStart]) {
        [[HmCloudTool share] configWithParams:call.arguments];
        [HmCloudTool share].isAudience = NO;
        [[HmCloudTool share] startGame];
    }

    if ([call.method isEqualToString:MethodControlPlay]) {
        [[HmCloudTool share] configWithParams:call.arguments];
        [HmCloudTool share].isAudience = YES;
        [[HmCloudTool share] registWithDelegate:self];
//        [[HmCloudTool share] startGame];
    }

    if ([call.method isEqualToString:MethodPlayPartyInfo]) {
        NSLog(@"123");
    }

    if ([call.method isEqualToString:MethodExitQueue]) {
        [[HmCloudTool share] onlyStop];
    }

    if ([call.method isEqualToString:MethodClosePage]) {
        [[HmCloudTool share] restart];
    }

    // 收到更新时间action的时候，通知flutter端计算用户的游玩时长，并更新插件
    if ([call.method isEqualToString:MethodBuySuccess]) {
        [self sendToFlutter:ActionUpdateTime params:nil];
    }

    if ([call.method isEqualToString:MethodUpdatePlayInfo]) {
        [[HmCloudTool share] updatePlayInfo:call.arguments];
    }

    if ([call.method isEqualToString:MethodGetUnReleaseGame]) {
        [[HmCloudTool share] getUnReleaseGame:^(NSDictionary *_Nonnull dict) {
            result(dict);
        }];
    }

    if ([call.method isEqualToString:MethodGetArchiveProgress]) {
        [[HmCloudTool share] getArchiveResult:^(BOOL isSucc) {
            result(@(isSucc));
        }];
    }

    if ([call.method isEqualToString:MethodReleaseGame]) {
        [[HmCloudTool share] releaseGame:^(BOOL isSucc) {
            result(@(isSucc));
        }
                              withParams:call.arguments];
    }
}

- (void)sendToFlutter:(NSString *)actionName params:(id _Nullable)params {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.channel invokeMethod:actionName arguments:params];
    });
}

@end
