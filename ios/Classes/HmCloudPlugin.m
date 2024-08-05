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
    if ([call.method isEqualToString:MethodStart]) {
        [[HmCloudTool share] configWithParams:call.arguments];

        [[HmCloudTool share] registWithDelegate:self];
    }

    if ([call.method isEqualToString:MethodExitQueue]) {
        [[HmCloudTool share] stop];
    }
}

- (void)sendToFlutter:(NSString *)actionName params:(id _Nullable)params {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.channel invokeMethod:actionName arguments:params];
    });
}

@end
