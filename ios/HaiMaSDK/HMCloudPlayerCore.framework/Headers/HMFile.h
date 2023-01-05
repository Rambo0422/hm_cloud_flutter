//
//  HMFile.h
//  HMCloudPlayerSDK
//
//  Created by apple on 2022/5/30.
//  Copyright © 2022 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface HMFile : NSObject

- (instancetype) initFile:(NSString *)fileName destinationPath:(NSString *)destinationPath;

@property(nonatomic,copy)   NSString *fileName;
@property(nonatomic,copy)   NSString *destinationPath;
@property(nonatomic,copy)   NSString *fileSize;
@property(nonatomic,copy)   NSString *mimeType;
@property(nonatomic,copy)   NSString *md5sum;
@property(nonatomic,assign) BOOL      isCancel;
@end

NS_ASSUME_NONNULL_END
