//
//  HMLogger.h
//  SDKDemoFull
//
//  Created by 王堪龙 on 2024/7/15.
//  Copyright © 2024 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

#define HMLOGGER_ENABLED 0

#if (HMLOGGER_ENABLED == 0)
#ifndef DEBUG
#define NSLog(FORMAT, ...) nil
#endif
#else
#define NSLog(format, ...) [HMLogger logd:format,##__VA_ARGS__];
#endif

typedef void(^HMLoggerBlock)(NSString* message);

@interface HMLogger : NSObject

+ (void) install;

+ (void) logd:(NSString *)format, ...;

+ (void) addBlock:(HMLoggerBlock)block;

+ (void) removeBlock:(HMLoggerBlock)block;

@end

NS_ASSUME_NONNULL_END
