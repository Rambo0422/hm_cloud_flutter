//
//  JoystickArrowView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import <UIKit/UIKit.h>
#import "BaseKeyView.h"
NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    DirectionLeft,
    DirectionTopLeft,
    DirectionTop,
    DirectionTopRight,
    DirectionRight,
    DirectionBottomRight,
    DirectionBottom,
    DirectionBottomLeft,
    DirectionNormal,
} Direction;

typedef void (^DirectionCallback)(Direction oldD, Direction newD);

@interface JoystickArrowView : BaseKeyView

@property (nonatomic, strong) UIImage *bgImg;
@property (nonatomic, strong) UIImage *thumbImg;
@property (nonatomic, strong) DirectionCallback callback;

@end

NS_ASSUME_NONNULL_END
