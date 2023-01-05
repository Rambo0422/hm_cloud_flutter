//
//  HMCCBaseModel.h
//  HMCloudCore
//
//  Created by Apple on 2018/5/16.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HMCCBaseModel : NSObject

+ (instancetype) instanceWithDictionary:(NSDictionary *)dict;
- (instancetype) initWitDictionary:(NSDictionary *)dict;

+ (instancetype) instanceWithKVArray:(NSArray *)arr kKey:(NSString *)kKey vKey:(NSString *)vKey;
+ (NSDictionary *) modelToDictionary:(id)model;

@end

@protocol HMCCBaseModelDelegate

@optional
+ (NSDictionary *) replaceKeyFromPerpoertyName;

#if TARGET_CPU_ARM
+ (BOOL) isBoolFromNSString:(NSString *)key;
#endif

@end
