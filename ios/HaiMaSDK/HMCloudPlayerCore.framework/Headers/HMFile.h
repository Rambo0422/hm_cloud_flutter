//
//  HMFile.h
//  HMCloudPlayerSDK
//
//  Created by apple on 2022/5/30.
//  Copyright © 2022 Apple. All rights reserved.
//

#import "HMCCBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface HMFile : HMCCBaseModel

/*
 通用
 */
@property (nonatomic, copy)   NSString *destinationPath;
@property (nonatomic, assign) BOOL      isCancel;

/*
 图片下载
 */
- (instancetype) initFile:(NSString *)fileName destinationPath:(NSString *)destinationPath;

@property (nonatomic, copy)   NSString *fileSize;
@property (nonatomic, copy)   NSString *fileName;
@property (nonatomic, copy)   NSString *mimeType;
@property (nonatomic, copy)   NSString *md5sum;

/*
 视频下载
 */
@property (nonatomic, copy, readonly)   NSString *name;
@property (nonatomic, copy, readonly)   NSString *path;
@property (nonatomic, copy, readonly)   NSString *size;
@property (nonatomic, copy, readonly)   NSString *type;
@property (nonatomic, copy, readonly)   NSString *duration;
@property (nonatomic, copy)             NSString *keepAliveTime;
@property (nonatomic, assign)           BOOL      downloadOnly;  // true 断流下载  false不断流下载

@end

NS_ASSUME_NONNULL_END
