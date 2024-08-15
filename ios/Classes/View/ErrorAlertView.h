//
//  ErrorAlertView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ErrorAlertView : UIView

+ (void)showAlertWithCid:(nullable NSString *)cid uid:(nullable NSString *)uid errorCode:(nullable NSString *)errorCode title:(nullable NSString *)title content:(nullable NSString *)content dissMissCallback:(nullable void (^)(void))callback;

@end

NS_ASSUME_NONNULL_END
