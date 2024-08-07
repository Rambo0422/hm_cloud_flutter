//
//  JoystickView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^PointCallback)(CGPoint point);

@interface JoystickView : UIView

@property (nonatomic, strong) UIImage *bgImg;
@property (nonatomic, strong) UIImage *thumbImg;

@property (nonatomic, strong) PointCallback callback;

@end

NS_ASSUME_NONNULL_END
