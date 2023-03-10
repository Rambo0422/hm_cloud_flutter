//
//  CloudPreViewController.h
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import <UIKit/UIKit.h>
#import <Flutter/Flutter.h>
NS_ASSUME_NONNULL_BEGIN

#define k_changeSound       @"changeSound"
#define k_startSuccess      @"startSuccess"
#define k_startFailed       @"startFailed"
#define k_cloudInitBegan    @"cloudInitBegan"
#define k_cloudQueueInfo    @"cloudQueueInfo"
#define k_videoVisble       @"videoVisble"
#define k_videoFailed       @"videoFailed"

#define k_DaShenBundle [NSBundle bundleWithPath:[[NSBundle bundleForClass:self.class] pathForResource:@"DaShen" ofType:@"bundle"]]
#define k_BundleImage(name) [UIImage imageWithContentsOfFile:[k_DaShenBundle pathForResource:name ofType:@"png"]]

@interface CloudPreViewController : UIViewController

@property (nonatomic, strong) UIViewController *gameVC;

@property (nonatomic, strong) void(^didDismiss)(void);
@property (nonatomic, strong) void(^channelAction)(NSString *methodName, bool value);


@end

NS_ASSUME_NONNULL_END
