//
//  PartyAvatarTableViewCell.h
//  hm_cloud
//
//  Created by a水 on 2024/11/1.
//

#import <UIKit/UIKit.h>
#import "PartyAvatarModel.h"
NS_ASSUME_NONNULL_BEGIN

@interface PartyAvatarTableViewCell : UITableViewCell

@property (nonatomic, strong) PartyAvatarModel *model;


- (void)configViewWithModel:(PartyAvatarModel *)model index:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
