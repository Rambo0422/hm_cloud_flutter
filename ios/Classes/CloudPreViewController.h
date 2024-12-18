//
//  CloudPreViewController.h
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import "HM_BaseViewController.h"
NS_ASSUME_NONNULL_BEGIN


typedef enum : NSUInteger {
    Flutter_rechartTime,
    Flutter_rechartVip
} FlutterPageType;


@interface CloudPreViewController : HM_BaseViewController

@property (nonatomic, strong) UIViewController *gameVC;

@property (nonatomic, strong) void (^ didDismiss)(void);
@property (nonatomic, strong) void (^ pushFlutter)(FlutterPageType pageType);

- (void)stopTimer;


/// 刷新延迟信息
/// - Parameters:
///   - fps: 帧率
///   - ms: 延迟
///   - rate: 码率
///   - packetLoss: 丢包率
- (void)refreshfps:(NSInteger)fps ms:(NSInteger)ms rate:(float)rate packetLoss:(float)packetLoss;


/// 刷新键盘状态
/// - Parameter status: 键盘状态
- (void)refreshKeyboardStatus:(CloudPlayerKeyboardStatus)status;

@end

NS_ASSUME_NONNULL_END
