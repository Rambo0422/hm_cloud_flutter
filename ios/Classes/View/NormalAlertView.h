//
//  NormalAlertView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NormalAlertView : UIView

+ (void)showAlertWithTitle:(nullable NSString *)title
                   content:(nullable NSString *)content
              confirmTitle:(nullable NSString *)confirmTitle
               cancelTitle:(nullable NSString *)cancelTitle
           confirmCallback:(nullable void (^)(void))confirm
            cancelCallback:(nullable void (^)(void))cancel;

@end

NS_ASSUME_NONNULL_END
