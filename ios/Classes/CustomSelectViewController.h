//
//  CustomSelectViewController.h
//  hm_cloud-SanA_Game
//
//  Created by a水 on 2024/8/9.
//

#import "HM_BaseViewController.h"
#import "SelectKeyCollectionViewCell.h"
NS_ASSUME_NONNULL_BEGIN

@interface CustomSelectViewController : HM_BaseViewController

@property (nonatomic, strong) void (^ useCallback)(KeyDetailModel *model);

//@property (nonatomic, strong) void (^ addCallback)(KeyDetailModel *model, BOOL isEdit);

@property (nonatomic, strong) void (^ pushVipCallback)(void);

@end

NS_ASSUME_NONNULL_END
