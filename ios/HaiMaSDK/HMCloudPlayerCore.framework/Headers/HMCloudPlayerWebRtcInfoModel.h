//
//  HMCloudPlayerWebRtcInfoModel.h
//  HMCloudPlayerCore
//
//  Created by apple on 2020/6/16.
//  Copyright © 2020 Apple. All rights reserved.
//

#import "HMStreamingModel.h"

@interface HMCloudPlayerWebRtcInfoModel : HMStreamingModel
@property (nonatomic, copy)   NSString *coTurnUrl;
@property (nonatomic, copy)   NSString *roomId;
@property (nonatomic, copy)   NSString *signalUrl;
@property (nonatomic, copy)   NSString *signalV2Url;
@property (nonatomic, copy)   NSString *inputUrl;
@property (nonatomic, copy)   NSString *screenUrl;
@property (nonatomic, assign) int       supportLiving;
@property (nonatomic, copy)   NSString *gsmUrl;
@property (nonatomic, copy)   NSString *screenRecordUrl;
@property (nonatomic, copy)   NSNumber *deviceId;
@property (nonatomic, copy)   NSString *userId;
@property (nonatomic, copy)   NSString *sdkVersion;

- (BOOL) isValid;
@end
