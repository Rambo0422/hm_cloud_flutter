//
//  PartyAvatarModel.h
//  hm_cloud
//
//  Created by a水 on 2024/10/31.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PartyAvatarModel : NSObject


@property (nonatomic, strong)  NSString *avatar_url;
@property (nonatomic, assign)  NSInteger index;
@property (nonatomic, strong)  NSString *nickname;
@property (nonatomic, assign)  NSInteger status;
@property (nonatomic, strong)  NSString *uid;

// 是否拥有玩的权限 这个要根据controlInfos里面的数据来遍历处理
@property (nonatomic, assign)  BOOL isPermission;

@end

NS_ASSUME_NONNULL_END
