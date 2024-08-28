//
//  HMCloudPlayerResolution.h
//  HMCloudPlayerCore
//
//  Created by Apple on 2018/5/16.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import "HMCCBaseModel.h"

@interface HMCloudPlayerResolution : HMCCBaseModel

@property (nonatomic, assign)   NSInteger   resolutionId;
@property (nonatomic, copy)     NSString    *name;
@property (nonatomic, assign)   NSInteger   frameRate;      //帧率
@property (nonatomic, assign)   NSInteger   peakBitRate;    //峰值码率 单位KB
@property (nonatomic, assign)   BOOL        defaultChoice;  //是否是默认选择， 0为未选择，1为选择
@property (nonatomic, copy)     NSString    *resolution;    // 分辨率，如-1280x720

@end
