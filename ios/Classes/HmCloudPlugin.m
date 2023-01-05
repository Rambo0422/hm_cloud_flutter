#import "HmCloudPlugin.h"
#import "HmCloudViewFactory.h"
#import "HmCloudPlatformView.h"

@interface HmCloudPlugin ()

@end

@implementation HmCloudPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"hm_cloud"
                                     binaryMessenger:[registrar messenger]];
    
    HmCloudPlugin* instance = [[HmCloudPlugin alloc] init];
    
    [registrar registerViewFactory:[[HmCloudViewFactory alloc] initWithMessenger:registrar.messenger] withId:@"hmCloudView"];
    
    [registrar addMethodCallDelegate:instance channel:channel];
    
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"getPlatformVersion" isEqualToString:call.method]) {
        result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
    }else if ([@"getBatteryLevel" isEqualToString:call.method]) {
        CGFloat batteryLevel = [self getBatteryLevel];
        result([NSString stringWithFormat:@"%.0f%%",batteryLevel]);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (CGFloat)getBatteryLevel{

    [UIDevice currentDevice].batteryMonitoringEnabled = YES;
    
    if ([UIDevice currentDevice].batteryState == UIDeviceBatteryStateUnknown) {
        return -1;
    }
    return (CGFloat)[UIDevice currentDevice].batteryLevel*100;
}

@end
