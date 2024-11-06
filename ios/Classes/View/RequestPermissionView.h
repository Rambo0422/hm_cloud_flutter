//
//  RequestPermissionView.h
//  hm_cloud
//
//  Created by a水 on 2024/11/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface RequestPermissionView : UIView
+ (instancetype)share;
- (void)showRequest:(NSDictionary *)dict inView:(UIView *)view;

@property (nonatomic, strong) void (^ letPlayCallback)(NSString *uid);

@end

NS_ASSUME_NONNULL_END
