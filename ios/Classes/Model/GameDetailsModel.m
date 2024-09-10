//
//  GameDetailsModel.m
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import "GameDetailsModel.h"

@implementation GameDetailsModel

- (NSInteger)support {
    if (_support == 0) {
        return 3;
    } else {
        return _support;
    }
}

@end
