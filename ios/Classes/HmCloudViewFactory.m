//
//  HmCloudViewFactory.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/3.
//

#import "HmCloudViewFactory.h"
#import "HmCloudPlatformView.h"

@interface HmCloudViewFactory ()


@property(nonatomic)NSObject<FlutterBinaryMessenger>* messenger;

@end

@implementation HmCloudViewFactory


- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
    if (self) {
        self.messenger = messenger;
    }
    return self;
}

-(NSObject<FlutterMessageCodec> *)createArgsCodec{
    return [FlutterStandardMessageCodec sharedInstance];
}

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id _Nullable)args {
    
    return [[HmCloudPlatformView alloc] initWithFrame:frame viewId:viewId args:args messager:self.messenger];
}

@end
