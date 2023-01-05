//
//  HMCloudPlayerWebRtcInfoModel.h
//  HMCloudPlayerCore
//
//  Created by apple on 2020/6/16.
//  Copyright © 2020 Apple. All rights reserved.
//

#import "HMStreamingModel.h"

@interface HMCloudPlayerWebRtcInfoModel : HMStreamingModel
@property (nonatomic, strong) NSString *coTurnUrl;
@property (nonatomic, strong) NSString *roomId;
@property (nonatomic, strong) NSString *signalUrl;
@property (nonatomic, strong) NSString *inputUrl;
@property (nonatomic, strong) NSString *screenUrl;
@property (nonatomic, assign) int       supportLiving;
@property(nonatomic,copy)     NSString *gsmUrl;

- (BOOL) isValid;
@end
