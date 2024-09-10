//
//  CustomSelectViewController.h
//  hm_cloud-SanA_Game
//
//  Created by a水 on 2024/8/9.
//

#import "HM_BaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface CustomSelectViewController : HM_BaseViewController

/// 选择自定义的模式 1 = 键鼠 2 = 手柄
@property (nonatomic, strong) void (^ selectCallback)(CustomType type);

@end

NS_ASSUME_NONNULL_END
