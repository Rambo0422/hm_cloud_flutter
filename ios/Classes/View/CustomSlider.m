//
//  CustomSlider.m
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import "CustomSlider.h"

@implementation CustomSlider

// 重写 trackRect(forBounds:) 方法
- (CGRect)trackRectForBounds:(CGRect)bounds {
    // 调用父类的实现来获取默认的轨道 rect
    CGRect defaultRect = [super trackRectForBounds:bounds];

    // 修改轨道的高度
    CGRect customRect = CGRectMake(defaultRect.origin.x, (bounds.size.height - 10) / 2, defaultRect.size.width, 10); // 10 是自定义的高度

    return customRect;
}

@end
