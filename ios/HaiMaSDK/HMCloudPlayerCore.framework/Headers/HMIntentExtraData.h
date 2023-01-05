//
//  HMIntentExtraData.h
//  HMCloudCore
//
//  Created by  张恒海 on 2021/4/16.
//  Copyright © 2021 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface HMIntentExtraData : NSObject

@property (nonatomic, strong) NSDictionary *booleanExtra;
@property (nonatomic, strong) NSDictionary *integerExtra;
@property (nonatomic, strong) NSDictionary *integerArrayExtra;
@property (nonatomic, strong) NSDictionary *integerListExtra;
@property (nonatomic, strong) NSDictionary *stringExtra;
@property (nonatomic, strong) NSDictionary *stringArrayExtra;
@property (nonatomic, strong) NSDictionary *stringListExtra;
@property (nonatomic, strong) NSDictionary *floatExtra;
@property (nonatomic, strong) NSDictionary *floatArrayExtra;
@property (nonatomic, strong) NSDictionary *floatListExtra;
@property (nonatomic, strong) NSDictionary *longExtra;
@property (nonatomic, strong) NSDictionary *longArrayExtra;
@property (nonatomic, strong) NSDictionary *longListExtra;
@property (nonatomic, strong) NSDictionary *componentNameExtra;
@property (nonatomic, strong) NSDictionary *uriExtra;
@property (nonatomic, copy)   NSString     *appLink;

- (NSDictionary *) getIntentExtraData;
- (NSDictionary *) getAppLinkData;

@end

NS_ASSUME_NONNULL_END
