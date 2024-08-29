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
    CloudPlayerWSMessageTypeShareLink,
    CloudPlayerWSMessageTypeDownloadFile,
    CloudPlayerWSMessageTypeStorage,
    CloudPlayerWSMessageTypeKeyboardChanged,
    CloudPlayerWSMessageTypeOpenCamera,
    CloudPlayerWSMessageTypeOpenPhotosAlbum,
    CloudPlayerWSMessageTypeCloudIme,
    CloudPlayerWSMessageTypeShellCommonds,
};

typedef NS_ENUM (NSInteger,CloudPlayerWSMessageIMEType) {
    CloudPlayerWSMessageIMETypeCloud,
    CloudPlayerWSMessageIMETypeLocal,
};

typedef NS_ENUM(NSInteger, HMCloudPlayerWSMessageInstanceType) {
    HMCloudPlayerWSMessageInstanceTypeUnknown        = -1, //无法确定
    HMCloudPlayerWSMessageInstanceTypeArm            =  0, //arm
    HMCloudPlayerWSMessageInstanceTypeX86            =  1, //x86
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
@property (nonatomic, assign) HMCloudPlayerWSMessageInstanceType instanceType;


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

@property (nonatomic, assign) NSInteger maxLength;

- (NSArray<HMCloudPlayerWSClipboardMessageItem*> *) clipboardItems;

@end

@interface HMCloudPlayerWSMessageAckResponse : NSObject

+ (instancetype) cloudPayerWSMessageAckWithDictionary:(NSDictionary *)dict;

+ (instancetype) cloudPayerWSMessageAckWithCloudId:(NSString *)cloudId type:(CloudPlayerWSMessageType)type messageId:(NSString *)messageId;

@property (nonatomic, copy, readonly) NSString *messageId;
@property (nonatomic, copy)     NSString *cloudId;
@property (nonatomic, assign)   int      code;
@property (nonatomic, copy)     NSString *message;
@property (nonatomic, assign)   CloudPlayerWSMessageType type;

- (BOOL) isSuccessed;

- (NSString *) jsonString;

@end

@interface HMCloudPlayerWSMessageFileAckResponse : HMCloudPlayerWSMessageAckResponse

+ (instancetype)cloudPayerWSMessageFileAckWithCloudId:(NSString *)cloudId bid:(NSString *)bid type:(CloudPlayerWSMessageType)type messageId:(NSString *)messageId event:(NSString *)event fileName:(NSString *)fileName;

@end

typedef NS_ENUM(NSInteger, CloudPlayerWSKeyboardMessage) {
    CloudPlayerWSKeyboardMessageUnknown = -1,   //无法确定
    CloudPlayerWSKeyboardMessageDidShow,        //显示
    CloudPlayerWSKeyboardMessageDidHide,        //隐藏
} ;

@interface HMCloudPlayerWSKeyboardChangedMessage : HMCloudPlayerWSMessage

@property (nonatomic, assign, readonly) CloudPlayerWSKeyboardMessage keyboardStatus;
@property (nonatomic, copy, readonly)   NSString *message;

@end

@interface HMCloudPlayerWSMessageFactroy : NSObject
+ (HMCloudPlayerWSMessage *)wsMessageFactroyWithDict:(NSDictionary *)dict;
@end

@interface HMCloudPlayerWSCloudIMEMessage : HMCloudPlayerWSMessage
@property(nonatomic,copy)NSString *imeTypeContent;
@property(nonatomic,assign)CloudPlayerWSMessageIMEType imeType;
@end

@interface HMCloudPlayerWSShellCommondsMessage : HMCloudPlayerWSMessage
+ (instancetype) cloudPlayerWSShellCommonds:(NSString *)shellCommonds;
@property (nonatomic, copy) NSString *shellCommonds;
@end

NS_ASSUME_NONNULL_END
