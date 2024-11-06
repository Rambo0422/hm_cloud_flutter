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
@property (nonatomic, strong) void (^ letPlayCallback)(NSString *uid);
@property (nonatomic, strong) void (^ closeUserPlayCallback)(NSString *uid);
@property (nonatomic, strong) void (^ wantPlayCallback)(NSString *uid);
@property (nonatomic, strong) void (^ kickoutCallback)(NSString *uid);


- (void)configViewWithModel:(PartyAvatarModel *)model index:(NSInteger)index;



@end

NS_ASSUME_NONNULL_END
