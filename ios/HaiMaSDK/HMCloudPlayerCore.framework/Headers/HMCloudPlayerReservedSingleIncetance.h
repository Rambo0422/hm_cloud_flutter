//
//  HMCloudPlayerReservedSingleIncetance.h
//  HMCloudPlayerSDK
//
//  Created by apple on 2019/11/22.
//  Copyright © 2019 Apple. All rights reserved.
//

#import "HMCCBaseModel.h"

@interface HMCloudPlayerReservedSingleIncetance : HMCCBaseModel

@property (nonatomic, copy) NSString *cid;
@property (nonatomic, copy) NSString *pkgName;
@property (nonatomic, copy) NSString *gameName;
@property (nonatomic, copy) NSString *appChannel;

@end
