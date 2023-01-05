//
//  HMStreamingModel.h
//  HMCloudCore
//
//  Created by apple on 2020/6/17.
//  Copyright © 2020 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "HMCloudCoreMarco.h"
@protocol HMStreamingModelDelegate

@optional
+ (NSDictionary *) replaceKeyFromPerpoertyName;

#if TARGET_CPU_ARM
+ (BOOL) isBoolFromNSString:(NSString *)key;
#endif

@end


@interface HMStreamingModel : NSObject

+ (instancetype) instanceWithDictionary:(NSDictionary *)dict;
- (instancetype) initWitDictionary:(NSDictionary *)dict;

+ (instancetype) instanceWithKVArray:(NSArray *)arr kKey:(NSString *)kKey vKey:(NSString *)vKey;
+ (NSDictionary *) modelToDictionary:(id)model;

@property (nonatomic, assign) CGSize showViewSize;
@property (nonatomic, assign) BOOL isRotating;
@property (nonatomic, assign) BOOL isHidden;
@property (nonatomic, assign) BOOL isShowViewSize;

@property (nonatomic, assign) HMCloudCorePlayerOrientation playerOrientation;
@property (nonatomic, strong) NSString *showViewSizeStr;

@end
