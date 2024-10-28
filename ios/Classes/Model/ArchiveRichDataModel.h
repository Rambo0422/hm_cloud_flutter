//
//  ArchiveRichDataModel.h
//  hm_cloud
//
//  Created by a水 on 2024/10/25.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ArchiveRichDataModel : NSObject

@property (nonatomic, assign)  NSInteger cid;
@property (nonatomic, strong)  NSString *downloadUrl;
@property (nonatomic, strong)  NSString *format;
@property (nonatomic, strong)  NSString *gameId;
@property (nonatomic, strong)  NSString *md5;
@property (nonatomic, assign)  BOOL thirdParty;
@property (nonatomic, assign)  BOOL uploadArchive;

@end

NS_ASSUME_NONNULL_END
