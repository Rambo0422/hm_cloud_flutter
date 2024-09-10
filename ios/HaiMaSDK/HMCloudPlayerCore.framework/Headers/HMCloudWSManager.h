//
//  HMCloudWSManager.h
//  HMCloudPlayerCore
//
//  Created by Apple on 2021/9/1.
//  Copyright © 2021 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol HMCloudWSManagerDelegate;

typedef NS_ENUM (NSInteger, CloudWSManagerProto) {
    CloudWSManagerProtoDefault = 0, //ScreenUrl : 屏幕方向/录音
    CloudWSManagerProtoTransfer,    //ScreenUrl : 体感数据
};

@interface HMCloudWSManager : NSObject

@property (nonatomic, weak) id<HMCloudWSManagerDelegate> delegate;


+ (instancetype) cloudWSManagerWithBaseURL:(NSString *)url;

- (BOOL) open:(CloudWSManagerProto)proto;

- (BOOL) sendText:(CloudWSManagerProto)proto text:(NSString *)text;
- (BOOL) sendData:(CloudWSManagerProto)proto data:(NSData *)data;

- (void) close:(CloudWSManagerProto)proto;
- (void) closeAll;

@end

@protocol HMCloudWSManagerDelegate <NSObject>

- (void) wsManagerDidConnect:(CloudWSManagerProto)proto;
- (void) wsManagerDidDisconnect:(CloudWSManagerProto)proto error:(NSError*)error;
- (void) wsManager:(CloudWSManagerProto)proto didReceiveMessage:(NSString *)message;
- (void) wsManager:(CloudWSManagerProto)proto didReceiveData:(NSData *)data;

@end

NS_ASSUME_NONNULL_END
