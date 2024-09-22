//
//  ToastAlertView.h
//  hm_cloud
//
//  Created by a水 on 2024/9/22.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ToastAlertView : UIView

+ (void)showAlertWithTitle:(nullable NSString *)title
                   content:(nullable NSString *)content;

@end

NS_ASSUME_NONNULL_END
