#import "HmCloudPlugin.h"
#import "HmCloudTool.h"

@interface HmCloudPlugin ()

@end

@implementation HmCloudPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
                                     methodChannelWithName:@"hm_cloud_controller"
                                           binaryMessenger:[registrar messenger]];

    HmCloudPlugin *instance = [[HmCloudPlugin alloc] init];

    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([@"startCloudGame" isEqualToString:call.method]) {
        NSLog(@"%@", call.arguments);
        [[HmCloudTool share] configWithParams:call.arguments];
        [[HmCloudTool share] regist];
    }
}

@end
