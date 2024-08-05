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

// 横版
#define k_DelayInfo             @"delayInfo"
#define k_GameStop              @"gameStop"
#define k_FirstFrameArrival     @"firstFrameArrival"

#define k_SanABundle [NSBundle bundleWithPath:[[NSBundle bundleForClass:self.class] pathForResource:@"SanA_Game" ofType:@"bundle"]]
#define k_BundleImage(name) [UIImage imageNamed:name inBundle:k_SanABundle withConfiguration:nil]

@interface CloudPreViewController : UIViewController

@property (nonatomic, strong) UIViewController *gameVC;

@property (nonatomic, strong) void(^didDismiss)(void);
@property (nonatomic, strong) void(^pushFlutter)(void);


@end

NS_ASSUME_NONNULL_END
