//
//  KeyDetailModel.m
//  hm_cloud
//
//  Created by a水 on 2024/9/14.
//

#import "KeyDetailModel.h"
#import "SanA_Macro.h"

@implementation KeyDetailModel

+ (NSDictionary *)mj_replacedKeyFromPropertyName {
    return @{
        @"ID": @"_id"
    };
}

+ (NSDictionary *)mj_objectClassInArray {
    return @{
        @"keyboard": [KeyModel class],
    };
}

@end
