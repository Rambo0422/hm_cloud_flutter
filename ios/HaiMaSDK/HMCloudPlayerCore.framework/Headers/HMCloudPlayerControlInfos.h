//
//  HMCloudPlayerControlInfos.h
//  HMCloudPlayerCore
//
//  Created by apple on 2021/10/14.
//  Copyright © 2021 Apple. All rights reserved.
//

#import "HMCCBaseModel.h"


@interface HMCloudPlayerControlInfo : HMCCBaseModel
@property (nonatomic, copy) NSString *cid;
@property (nonatomic, assign) NSInteger position;
@end

@interface HMCloudPlayerControlInfos : HMCCBaseModel
@property (nonatomic,strong) NSArray *controlInfos;
@property (nonatomic,strong) NSArray <HMCloudPlayerControlInfo *> *controlInfoList;
@end
