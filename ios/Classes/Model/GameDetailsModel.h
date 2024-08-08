//
//  GameDetailsModel.h
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface GameDetailsModel : NSObject


/// 1 = 自定义按键 ； 2 = 自定义手柄； 3 = 两个都支持
@property (nonatomic, assign) NSInteger support;

@end

NS_ASSUME_NONNULL_END
