//
//  CloudPreViewController.h
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import <Flutter/Flutter.h>
#import <UIKit/UIKit.h>
#import "SanA_Macro.h"
NS_ASSUME_NONNULL_BEGIN



@interface CloudPreViewController : UIViewController

@property (nonatomic, strong) UIViewController *gameVC;

@property (nonatomic, strong) void (^ didDismiss)(void);
@property (nonatomic, strong) void (^ pushFlutter)(void);



/// 刷新延迟信息
/// - Parameters:
///   - fps: 帧率
///   - ms: 延迟
///   - rate: 码率
///   - packetLoss: 丢包率
- (void)refreshfps:(NSInteger)fps ms:(NSInteger)ms rate:(float)rate packetLoss:(float)packetLoss;

@end

NS_ASSUME_NONNULL_END
