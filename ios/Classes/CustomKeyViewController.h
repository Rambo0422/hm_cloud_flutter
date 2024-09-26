//
//  CustomKeyViewController.h
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "HM_BaseViewController.h"
#import "KeyDetailModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface CustomKeyViewController : HM_BaseViewController

@property (nonatomic, assign) CustomType type;

@property (nonatomic, assign) BOOL isEdit;
@property (nonatomic, strong) KeyDetailModel *model;


@property (nonatomic, strong) void (^ dismissCallback)(BOOL isRefresh);



@end

NS_ASSUME_NONNULL_END
