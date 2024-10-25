//
//  ArchiveRichDataModel.m
//  hm_cloud
//
//  Created by a水 on 2024/10/25.
//

#import "ArchiveRichDataModel.h"
#import "SanA_Macro.h"
@implementation ArchiveRichDataModel

+ (NSDictionary *)mj_replacedKeyFromPropertyName {
    return @{
        @"md5": @"fileMD5",
        @"downloadUrl": @"downLoadUrl"
    };
}

+ (void)initialize {
    [super initialize];
    [self mj_referenceReplacedKeyWhenCreatingKeyValues:NO];
}

@end
