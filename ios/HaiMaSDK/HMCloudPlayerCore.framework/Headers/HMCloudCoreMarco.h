//
//  HMCloudCoreMarco.h
//  HMCloudCore
//
//  Created by Apple on 2018/5/11.
//  Copyright © 2018年 Apple. All rights reserved.
//

#ifndef HMCloudCoreMarco_h
#define HMCloudCoreMarco_h

#import "HMLogger.h"

#define hm_dispatch_main_async_safe(block)\
if ([NSThread isMainThread]) {\
    block();\
} else {\
    dispatch_async(dispatch_get_main_queue(), block);\
}


typedef NS_ENUM(NSInteger, HMCloudCorePlayerOrientation) {
    HMCloudCorePlayerOrientationLandscape   = 0,    //横屏
    HMCloudCorePlayerOrientationPortrait    = 1,    //竖屏
};

typedef NS_ENUM(NSInteger, HMCloudPlayerUsageAuthorization){
    HMCloudPlayerUsageAuthorizationMicrophone = 0, //麦克风权限回调
    HMCloudPlayerUsageAuthorizationCamera          //相机权限回调
};

typedef NS_ENUM(NSInteger,HMCloudCorePlayerAudioSessionCategory) {
    HMCloudCorePlayerAudioSessionCategoryDefault,       // AVAudioSessionCategorySoloAmbient
    HMCloudCorePlayerAudioSessionCategoryPlayAndRecord, // AVAudioSessionCategoryPlayAndRecord
};


#endif /* HMCloudCoreMarco_h */
