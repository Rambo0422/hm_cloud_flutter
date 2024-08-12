//
//  FadeInOutAnimationController.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "FadeInOutAnimationController.h"

@implementation FadeInOutAnimationController

// 动画的持续时间
- (NSTimeInterval)transitionDuration:(id<UIViewControllerContextTransitioning>)transitionContext {
    return 0.5; // 动画时间为 0.5 秒
}

// 自定义动画逻辑
- (void)animateTransition:(id<UIViewControllerContextTransitioning>)transitionContext {
    // 获取要展示的视图控制器和它的视图
    UIViewController *toViewController = [transitionContext viewControllerForKey:UITransitionContextToViewControllerKey];
    UIViewController *fromViewController = [transitionContext viewControllerForKey:UITransitionContextFromViewControllerKey];

    UIView *containerView = [transitionContext containerView];

    // 判断是 present 还是 dismiss
    BOOL isPresenting = (toViewController.presentingViewController == fromViewController);

    if (isPresenting) {
        // Present 动画
        UIView *toView = toViewController.view;
        CGRect finalFrame = [transitionContext finalFrameForViewController:toViewController];

        // 设置初始状态，例如：目标视图从透明状态过渡到完全显示
        toView.frame = finalFrame;
        toView.alpha = 0.0;
        [containerView addSubview:toView];

        [UIView animateWithDuration:[self transitionDuration:transitionContext]
                         animations:^{
            toView.alpha = 1.0;
        }
                         completion:^(BOOL finished) {
            [transitionContext completeTransition:!transitionContext.transitionWasCancelled];
        }];
    } else {
        // Dismiss 动画
        UIView *fromView = fromViewController.view;

        [UIView animateWithDuration:[self transitionDuration:transitionContext]
                         animations:^{
            fromView.alpha = 0.0;
        }
                         completion:^(BOOL finished) {
            [fromView removeFromSuperview];
            [transitionContext completeTransition:!transitionContext.transitionWasCancelled];
        }];
    }
}

@end
