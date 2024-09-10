//
//  UIViewController+TopVc.m
//  hm_cloud
//
//  Created by a水 on 2024/7/31.
//

#import "UIViewController+TopVc.h"

@implementation UIViewController (TopVc)

+ (UIViewController *)topViewController {
    return [self topViewControllerWithRootViewController:[self currentRootViewController]];
}

+ (UIViewController *)currentRootViewController {
    for (UIWindowScene *windowScene in [UIApplication sharedApplication].connectedScenes) {
        if (windowScene.activationState == UISceneActivationStateForegroundActive) {
            for (UIWindow *window in windowScene.windows) {
                if (window.isKeyWindow) {
                    return window.rootViewController;
                }
            }
        }
    }

    return nil;
}

+ (UIViewController *)topViewControllerWithRootViewController:(UIViewController *)rootViewController {
    if (rootViewController.presentedViewController) {
        return [self topViewControllerWithRootViewController:rootViewController.presentedViewController];
    } else if ([rootViewController isKindOfClass:[UINavigationController class]]) {
        UINavigationController *navigationController = (UINavigationController *)rootViewController;
        return [self topViewControllerWithRootViewController:navigationController.visibleViewController];
    } else if ([rootViewController isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tabBarController = (UITabBarController *)rootViewController;
        return [self topViewControllerWithRootViewController:tabBarController.selectedViewController];
    } else {
        return rootViewController;
    }
}

@end
