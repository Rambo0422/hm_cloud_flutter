//
//  HMStreamingFileModel.h
//  HMCloudPlayerSDK
//
//  Created by apple on 2024/5/20.
//  Copyright © 2024 Apple. All rights reserved.
//

#import "HMStreamingModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface HMStreamingFileModel : HMStreamingModel

/*
 arm实例
 x86实例
 通用
 */
@property (nonatomic, copy)   NSString *destinationPath; //文件下载时存储本地目标路径，文件上传时本地源文件路径
@property (nonatomic, copy)   NSString *fileName;        //文件上传，文件下载时创建的文件名称
@property (nonatomic, assign) BOOL      isCancel;        //文件上传，文件下载取消标识
@property (nonatomic, copy)   NSString *cloudPath;       //文件上传指定相对路径

/**
 arm实例
 图片下载
 */
@property (nonatomic, copy)   NSString *fileSize; //图片下载接口，图片大小
@property (nonatomic, copy)   NSString *mimeType; //图片下载接口，图片类型
@property (nonatomic, copy)   NSString *md5sum;   //图片下载接口，md5值

/*
 arm实例
 文件下载相关属性
 */
@property (nonatomic, copy, readonly)   NSString *name; //文件名称
@property (nonatomic, copy, readonly)   NSString *size; //文件大小
@property (nonatomic, copy, readonly)   NSString *type; //文件类型
@property (nonatomic, copy, readonly)   NSString *duration;      //视频时长
@property (nonatomic, copy)             NSString *keepAliveTime; //保活时长
@property (nonatomic, assign)           BOOL      downloadOnly;  // true 断流下载  false不断流下载
@property (nonatomic, copy, readonly)   NSString *path; //云端文件相对路径

/*
 创建文件方法
 @param fileName 文件名
 @param destinationPath 文件下载时存储本地目标路径，文件上传时本地源文件路径
 */
- (instancetype) initFile:(NSString *)fileName destinationPath:(NSString *)destinationPath;

/**
 创建文件方法
 @param fileName 文件名
 @param destinationPath 文件下载时存储本地目标路径，文件上传时本地源文件路径
 @param cloudPath 指定云端相对路径
 */

- (instancetype) initFile:(NSString *)fileName destinationPath:(NSString *)destinationPath cloudPath:(NSString *)cloudPath;

- (NSDictionary *)toDictianary;

@end

NS_ASSUME_NONNULL_END
