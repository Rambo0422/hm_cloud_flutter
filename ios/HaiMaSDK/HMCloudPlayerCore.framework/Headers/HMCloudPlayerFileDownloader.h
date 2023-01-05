//
//  HMCloudPlayerFileDownloader.h
//  HMCloudPlayerSDK
//
//  Created by Apple on 2018/6/1.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void (^CPFileDownloaderResponseBlock)   (void);
typedef void (^CPFileDownloaderProgressBlock)   (unsigned long long totalBytes, NSData *data);
typedef void (^CPFileDownloaderDoneBlock)       (unsigned long long totalBytes, NSError *error);

@interface HMCloudPlayerFileDownloader : NSObject

+ (void) download:(NSString *)url
    responseBlock:(CPFileDownloaderResponseBlock)responseBlock
    progressBlock:(CPFileDownloaderProgressBlock)progressBlock
        doneBlock:(CPFileDownloaderDoneBlock)doneBlock
   timeoutSeconds:(NSInteger)timeout;

@end
