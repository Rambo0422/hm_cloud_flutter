//
//  PartyAvatarModel.m
//  hm_cloud
//
//  Created by a水 on 2024/10/31.
//

#import "PartyAvatarModel.h"

@implementation PartyAvatarModel

- (BOOL)isEqual:(id)object {
    if (![object isKindOfClass:[PartyAvatarModel class]]) {
        return NO;     // 类型不匹配
    }

    PartyAvatarModel *m = (PartyAvatarModel *)object;

    BOOL a = [self.avatar_url isEqualToString:m.avatar_url];

    if (self.avatar_url == nil && m.avatar_url == nil) {
        a = YES;
    }

    BOOL b =  self.index == m.index;

    BOOL c = [self.nickname isEqualToString:m.nickname];

    if (self.nickname == nil && m.nickname == nil) {
        c = YES;
    }

    BOOL d = self.status == m.status;

    BOOL e = [self.uid isEqualToString:m.uid];

    if (self.uid == nil && m.uid == nil) {
        e = YES;
    }

    BOOL f = self.isPermission == m.isPermission;

    return a && b && c && d && e && f;
}

@end
