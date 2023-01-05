//
//  HMCloudPlayerWSMessage.h
//  HMCloudPlayerCore
//
//  Created by Apple on 2021/6/30.
//  Copyright © 2021 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, CloudPlayerWSMessageType) {
    CloudPlayerWSMessageTypeUnsupported,
    CloudPlayerWSMessageTypeGPS,
    CloudPlayerWSMessageTypeIntent,
    CloudPlayerWSMessageTypeClipboard,
    CloudPlayerWSMessageTypeImageList,
    CloudPlayerWSMessageTypeScreenCap,
    CloudPlayerWSMessageTypeShareQQ,
    CloudPlayerWSMessageTypeShareWeiBo,
    CloudPlayerWSMessageTypeShareWeiXin,
    CloudPlayerWSMessageTypeShareAndroid,
    CloudPlayerWSMesaageTypeShareLink,
};

typedef void (^HMCPWSMessageBlock)(BOOL successed, NSString *messageId, NSString *reason);

extern NSString* const _CloudPlayerWSMessageKeyType;

@interface HMCloudPlayerWSMessage : NSObject

+ (instancetype) cloudPlayerWSMessageWithType:(CloudPlayerWSMessageType)type
                                  accessKeyId:(NSString *)accessKeyId
                                      cloudId:(NSString *)cloudId;

+ (instancetype) cloudPlayerWSMessageWithDictionary:(NSDictionary *)dict;

@property (nonatomic, assign, readonly) BOOL needAckResponse;
@property (nonatomic, assign, readonly) CloudPlayerWSMessageType type;
@property (nonatomic, assign, readonly) int timeoutSeconds;
@property (nonatomic, copy, readonly)   NSString *messageId;


- (BOOL) isValid;

- (NSString *) jsonString;

@end

typedef NS_ENUM(NSInteger, CloudPlayerWSGPSMessageOperation) {
    CloudPlayerWSGPSMessageOperationUnknown = 0,
    CloudPlayerWSGPSMessageOperationStart,
} ;

@interface HMCloudPlayerWSGPSMessage : HMCloudPlayerWSMessage

+ (instancetype) cloudPlayerWSGPSMessageWith:(NSString *)accessKeyId
                                     cloudId:(NSString *)cloudId
                                   longitude:(NSNumber *)longitude
                                    latitude:(NSNumber *)latitude;

+ (instancetype) cloudPlayerWSGPSMessageWith:(NSString *)accessKeyId
                                     cloudId:(NSString *)cloudId
                                   longitude:(NSNumber *)longitude
                                    latitude:(NSNumber *)latitude
                                    altitude:(NSNumber * _Nullable)altitude;

+ (instancetype) cloudPlayerWSGPSMessageWith:(NSString *)accessKeyId
                                     cloudId:(NSString *)cloudId
                                   longitude:(NSNumber *)longitude
                                    latitude:(NSNumber *)latitude
                                    altitude:(NSNumber * _Nullable)altitude
                                       speed:(NSNumber * _Nullable)speed;

+ (instancetype) cloudPlayerWSGPSMessageWith:(NSString *)accessKeyId
                                     cloudId:(NSString *)cloudId
                                   longitude:(NSNumber *)longitude
                                    latitude:(NSNumber *)latitude
                                    altitude:(NSNumber * _Nullable)altitude
                                       speed:(NSNumber * _Nullable)speed
                                      course:(NSNumber * _Nullable)course;

@property (nonatomic, assign, readonly) CloudPlayerWSGPSMessageOperation operation;

@end

@interface HMCloudPlayerWSIntentMessage : HMCloudPlayerWSMessage

+ (instancetype) cloudPlayerWSIntentMessageWith:(NSString *)accessKeyId
                                        cloudId:(NSString *)cloudId
                                         intent:(NSString *)intent;

@property (nonatomic, copy) NSDictionary *intent;

@end

@interface HMCloudPlayerWSClipboardMessageItem : NSObject

@property (nonatomic, copy, readonly) NSString *itemType;
@property (nonatomic, copy, readonly) NSString *itemData;

@end

@interface HMCloudPlayerWSClipboardMessage : HMCloudPlayerWSMessage

+ (instancetype) cloudPlayerWSClipboardMessage:(NSString *)accessKeyId
                                       cloudId:(NSString *)cloudId
                                          text:(NSString *)text;

- (NSArray<HMCloudPlayerWSClipboardMessageItem*> *) clipboardItems;

@end

@interface HMCloudPlayerWSMessageAckResponse : NSObject

+ (instancetype) cloudPayerWSMessageAckWithDictionary:(NSDictionary *)dict;

+ (instancetype) cloudPayerWSMessageAckWithCloudId:(NSString *)cloudId type:(CloudPlayerWSMessageType)type messageId:(NSString *)messageId;

@property (nonatomic, copy, readonly) NSString *messageId;

- (BOOL) isSuccessed;

- (NSString *) jsonString;

@end

NS_ASSUME_NONNULL_END
