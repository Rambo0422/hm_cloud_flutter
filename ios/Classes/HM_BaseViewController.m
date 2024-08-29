//
//  HM_BaseViewController.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "FadeInOutAnimationController.h"
#import "HM_BaseViewController.h"

@interface HM_BaseViewController ()

@property (nonatomic, strong) FadeInOutAnimationController *fadeInOutAnimationController;

@end

@implementation HM_BaseViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // 初始化动画控制器
    self.fadeInOutAnimationController = [FadeInOutAnimationController new];
}

- (UIRectEdge)preferredScreenEdgesDeferringSystemGestures {
    return UIRectEdgeAll;
}

- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    // 如果该界面需要支持横竖屏切换
    return UIInterfaceOrientationMaskLandscapeRight;
}

// 返回自定义的呈现动画控制器
- (id<UIViewControllerAnimatedTransitioning>)animationControllerForPresentedController:(UIViewController *)presented
                                                                  presentingController:(UIViewController *)presenting
                                                                      sourceController:(UIViewController *)source {
    return self.fadeInOutAnimationController;
}

- (id<UIViewControllerAnimatedTransitioning>)animationControllerForDismissedController:(UIViewController *)dismissed {
    return self.fadeInOutAnimationController;
}

@end
