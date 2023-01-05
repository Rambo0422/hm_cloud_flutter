//
//  HMCloudPlayer.h
//  HMCloudPlayerCore
//
//  Created by Apple on 2018/5/12.
//  Copyright © 2018年 Apple. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HMCloudCorePlayer.h"
#import "HMCloudPlayerReservedSingleIncetance.h"
#import "HMDelayInfoModel.h"
#import "HMIntentExtraData.h"
#import "HMCloudPlayerWSMessage.h"
#import "HMCloudWSManager.h"
#import "HMCloudPlayerControlInfos.h"
#import "HMScreenShotModel.h"
#import "HMFile.h"

@class HMCloudPlayerConfigModel;
@class HMCloudPlayerResolution;

@protocol HMCloudPlayerDelegate;

typedef NS_ENUM(NSInteger,CloudPlayerShareType) {
    CloudPlayerShareTypeQQ,
    CloudPlayerShareTypeWeiBo,
    CloudPlayerShareTypeWeiXin,
    CloudPlayerShareTypeAndroid,
    CloudPlayerShareTypeShareLink,
};

typedef NS_ENUM(NSInteger, CloudPlayerComponentType) {
    CloudPlayerComponentTypeNone         = -1,   //不传
    CloudPlayerComponentTypeActivity     = 0,   //Activity
    CloudPlayerComponentTypeService      = 1,   //Service
    CloudPlayerComponentTypeBroadcast    = 2,   //Broadcast
};


typedef NS_ENUM(NSInteger, CloudPlayerKeyboardStatus) {
    CloudPlayerKeyboardStatusNone       = -1,   //初始值
    CloudPlayerKeyboardStatusDidShow,           //显示
    CloudPlayerKeyboardStatusDidHide            //隐藏
};

typedef NS_ENUM(NSInteger, HMCloudPlayerKeyCommand){
    CloudPlayerKeyCommandBackGame   = 0x01, //一键返回游戏
    CloudPlayerKeyCommandEmptyTouch = 0x02, //"空操作"指令
};

typedef NS_ENUM(NSInteger, ELivingCapabilityStatus) {
    LivingUnknown     = -1,   //无法确定
    LivingUnSupported = 0,    //不支持直播
    LivingSupported   = 1,    //支持直播
};

typedef NS_ENUM(NSInteger, CloudInstanceType) {
    CloudInstanceTypeUnknown        = -1, //无法确定
    CloudInstanceTypeArm            =  0, //arm
    CloudInstanceTypeX86            =  1, //x86
};

typedef void (^HMReservedIncetanceCallback)(NSArray <HMCloudPlayerReservedSingleIncetance*>*);

typedef void (^HMCloudFileDownloadResponseBlock)(BOOL result, NSString *error, HMFile *file);

typedef void (^HMCloudFileDownloadComplete)(void);

typedef void (^HMCloudFileCancelDownloadResponseBlock)(BOOL result, NSString *error, HMFile *file);

typedef void (^HMCloudFileCancelDownloadComplete)(void);

const extern NSString *CLOUDGAME_SDK_VERSION;

//配置信息
const extern NSString *CloudGameConfigKeyAuthURL;
const extern NSString *CloudGameConfigKeyLinkURL;
const extern NSString *CloudGameConfigKeyCountlyURL;
const extern NSString *CloudGameConfigKeyCountlyKey;

//播流参数
const extern NSString *CloudGameOptionKeyConfigInfo;        //configInfo:免登相关
const extern NSString *CloudGameOptionKeyPriority;          //priority:排队优先级
const extern NSString *CloudGameOptionKeyPlayingTime;       //playingTime:用户本次可玩时间 单位：ms
const extern NSString *CloudGameOptionKeyArchive;           //isArchive:本次游戏是否存档 0-否 1-是
const extern NSString *CloudGameOptionKeyAppChannel;        //appChannel:游戏渠道号
const extern NSString *CloudGameOptionKeyProtoData;         //接入方透传数据字段
const extern NSString *CloudGameOptionKeyBitrate;           //起播码率，单位KB
const extern NSString *CloudGameOptionKeyCid;               //cid:重连时需要
const extern NSString *CloudGameOptionKeyUserType;          //用户类型
const extern NSString *CloudGameOptionKeyIPV6Supported;     //是否支持IPV6网络，即是否需要SDK查询IPV4地址
const extern NSString *CloudGameOptionKeyCidCacheInterval;  //缓存Cid超时时间，单位为秒，默认2小时
const extern NSString *CloudGameOptionKeyClearCachedCID;    //申请游戏前，是否清除本地缓存CID  0-否 1-是
const extern NSString *CloudGameOptionKeyAccessKeyId;       //AccessKeyId:支持多bid切换，跨bid移屏初始bid
const extern NSString *CloudGameOptionKeyArchiveFromUserId; //被读取文档用户userId
const extern NSString *CloudGameOptionKeyarchiveFromBid;    //被读取文档bid
const extern NSString *CloudGameOptionKeyClientISP;         //指定运营商
const extern NSString *CloudGameOptionKeyClientProvince;    //指定省份
const extern NSString *CloudGameOptionKeyClientCity;        //指定城市
const extern NSString *CloudGameOptionKeyStreamType;        //播流类型rtmp,webrtc
const extern NSString *CloudGameOptionKeyComponentType;     //组件类型
const extern NSString *CloudGameOptionKeyComponentAction;   //组件对应的Action
const extern NSString *CloudGameOptionKeyComponentName;     //组件名
const extern NSString *CloudGameOptionKeyIntentExtraData;   //IntentExtraData
const extern NSString *CloudGameOptionKeyUserDeviceInfo;    //设备信息
const extern NSString *CloudGameOptionKeyLanguage;          //设置语言
const extern NSString *CloudGameOptionKeyRichData;          //透传到SAAS的字段
const extern NSString *CloudGameOptionKeyResolutionId;      //起播分辨率

@interface HMCloudPlayer : HMCloudCorePlayer

@property (nonatomic, weak)             id<HMCloudPlayerDelegate> delegate;
@property (nonatomic, copy, readonly)   NSString                  *videoUrl;
@property (nonatomic, assign, readonly) NSInteger                 playingTime;


/**
 配置云游戏连接信息；该方法在创建单例后第一时间调用

 @param info 连接信息 AUTH / Countly/ Link
 @return 是否成功 //参数类型非法，或者已经请求过SAAS地址，均返回失败。
 */
- (BOOL) config:(NSDictionary *)info;

/**
IDC节点测速

@param seconds 最大测速时长，单位：秒
@param ipv6Supported 是否支持IPV6，即：是否需要查询IPV4地址
@param accessKeyId 指定accessKeyId
@param success 测速成功回调
@param fail 测速失败回调
@return 是否开始测速
*/
- (BOOL) speedTest:(NSInteger)seconds ipv6Supported:(BOOL)ipv6Supported accessKeyId:(NSString *)accessKeyId success:(void (^)(int rst))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) speedTest:(NSInteger)seconds ipv6Supported:(BOOL)ipv6Supported success:(void (^)(int rst))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) speedTest:(NSInteger)seconds success:(void (^)(int rst))success fail:(void (^)(NSString *errorCode))fail;

/**
IDC路由查询

@param ipv6Supported 是否支持IPV6，即：是否需要查询IPV4地址
@param accessKeyId 指定accessKeyId
@param success 查询成功回调
@param fail 查询失败回调
@return 是否开始查询
*/
- (BOOL) idcQuery:(BOOL)ipv6Supported accessKeyId:(NSString *)accessKeyId success:(void (^)(NSString *url, NSString *ipAddress))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) idcQuery:(BOOL)ipv6Supported success:(void (^)(NSString *url, NSString *ipAddress))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) idcQuery:(void (^)(NSString *url, NSString *ipAddress))success fail:(void (^)(NSString *errorCode))fail;

- (void) setBitrate:(NSInteger)bitrate;

/**
 开始游戏
 */
- (void) play;

/**
 恢复云游戏

 @param playingTime 恢复后的时长，单位：ms
 */
- (void) resume:(NSInteger)playingTime;

/**
 手动切换清晰度

 @param resolutionId 目标清晰度ID
 */
- (void) switchResolution:(NSInteger)resolutionId;

/**
 确认进入排队队列
 */
- (void) confirmQueue;

/**
 发送特定键值指令，更新用户操作时间

 @param cmd 键值
 @return 是否发送
 */
- (BOOL) sendKeyCommand:(HMCloudPlayerKeyCommand)cmd;

/**
发送特定键值指令

@param cmd 键值
@param updateUserOperationTime  是否更新用户操作时间
@return 是否发送
*/
- (BOOL) sendKeyCommand:(HMCloudPlayerKeyCommand)cmd updateUserOperationTime:(BOOL)updateUserOperationTime;

/**
 Wi-Fi切换到4G网络的提示语
 */
- (NSString *) wifiTo4GTip;

/**
 游戏存档进度查询

 @param userId 必填
 @param userToken 必填
 @param pkgName 必填
 @param appChannel 选填
 @param accessKeyId 指定accessKeyId
 @param success 查询成功的回调
 @param fail 查询失败的回调
 @return 是否开始查询
 */
- (BOOL) gameArchiveQuery:(NSString *)userId userToken:(NSString *)userToken pkgName:(NSString *)pkgName appChannel:(NSString *)appChannel accessKeyId:(NSString *)accessKeyId success:(void (^)(BOOL finished))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) gameArchiveQuery:(NSString *)userId userToken:(NSString *)userToken pkgName:(NSString *)pkgName appChannel:(NSString *)appChannel success:(void (^)(BOOL finished))success fail:(void (^)(NSString *errorCode))fail;

/**
 游戏是否有存档查询

 @param userId 必填
 @param userToken 必填
 @param pkgName 必填
 @param appChannel 选填
 @param accessKeyId 指定accessKeyId
 @param success 查询成功的回调
 @param fail 查询失败的回调
 @return 是否开始查询
 */
- (BOOL) gameArchived:(NSString *)userId userToken:(NSString *)userToken pkgName:(NSString *)pkgName appChannel:(NSString *)appChannel accessKeyId:(NSString *)accessKeyId success:(void (^)(BOOL archived))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) gameArchived:(NSString *)userId userToken:(NSString *)userToken pkgName:(NSString *)pkgName appChannel:(NSString *)appChannel success:(void (^)(BOOL archived))success fail:(void (^)(NSString *errorCode))fail;

/**
  游戏过程中，更新UID和游戏时长

 @param userId 必填，不能为NULL
 @param userToken 必填，不能为NULL
 @param ctoken 必填，不能为NULL
 @param playingTime 必填，不能小于0，单位: ms
 @param tip 选填
 @param protoData 选填
 @param success 更新成功回调
 @param fail 更新失败回调
 @return 是否开始更新
 */
- (BOOL) updateGameUID:(NSString *)userId userToken:(NSString *)userToken ctoken:(NSString *)ctoken playingTime:(NSInteger)playingTime tip:(NSString *)tip protoData:(NSString *)protoData success:(void (^)(BOOL successed))success fail:(void (^)(NSString *errorCode))fail;

/**
 查询游戏配置信息（目前版本仅支持清晰度查询）

 @param pkgName 必填，不能为NULL
 @param appChannel 选填，可为NULL
 @param streamingType 必填
 @param accessKeyId 指定accessKeyId
 @param success 游戏配置的清晰度列表
 @param fail 查询失败回调
 @return 是否开始查询
 */
- (BOOL) gameParamsQuery:(NSString *)pkgName appChannel:(NSString *)appChannel streamingType:(CloudCoreStreamingType)streamingType accessKeyId:(NSString *)accessKeyId success:(void (^)(NSArray<HMCloudPlayerResolution *> *))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL) gameParamsQuery:(NSString *)pkgName appChannel:(NSString *)appChannel streamingType:(CloudCoreStreamingType)streamingType success:(void (^)(NSArray<HMCloudPlayerResolution *> *))success fail:(void (^)(NSString *errorCode))fail;

/**
 云游戏提供静音接口
 @param mute 静音
 */
- (void) setAudioMute:(BOOL)mute;

/**
 获取某一秒延迟检测信息
 @return 包含延迟信息的HMDelayInfoModel
 */
- (HMDelayInfoModel *) getDelayInfo;

/**
 查询是否有上次未完成的游戏实例
 @param options 游戏参数  {CloudGameOptionKeyUserId:@"...", CloudGameOptionKeyUserToken:"...",CloudGameOptionKeyAccessKeyId:"..."}
 @param reservedIncetance 查询驻留机回调方法，包含驻留机数组
 */
- (void) getReservedInstance:(NSDictionary *)options ReservedIncetance:(HMReservedIncetanceCallback)reservedIncetance;

/**
 根据cid立即释放实例
 @param cid 必填，不能为NULL
 @param ctoken 必填，不能为NULL,并且必须与之前播放的实例传入的cToken一致
 @param userId 必填，不能为NULL
 @param userToken 必填，不能为NULL
 @param pkgName 必填，不能为NULL
 @param appChannel 选填，可为NULL
 @param accessKeyId 指定accessKeyId
 @param success 释放成功回调
 @param fail 释放失败回调
 @return 是否开始释放实例
*/
- (BOOL)gameReleaseInstanceWithCid:(NSString *)cid ctoken:(NSString *)ctoken userId:(NSString *)userId userToken:(NSString *)userToken pkgName:(NSString *)pkgName appChannel:(NSString *)appChannel accessKeyId:(NSString *)accessKeyId success:(void(^)(BOOL released))success fail:(void (^)(NSString *errorCode))fail;

- (BOOL)gameReleaseInstanceWithCid:(NSString *)cid ctoken:(NSString *)ctoken userId:(NSString *)userId userToken:(NSString *)userToken pkgName:(NSString *)pkgName appChannel:(NSString *)appChannel success:(void(^)(BOOL released))success fail:(void (^)(NSString *errorCode))fail;

/**
 游戏过程中，开启直播

 @param livingId 直播唯一标识，必填，不能为NULL
 @param pushStreamUrl 第三方推流地址，必填，不能为NULL
 @return 是否开始开启直播
 */
- (BOOL) startLivingWithLivingId:(NSString *)livingId pushStreamUrl:(NSString *)pushStreamUrl DEPRECATED_ATTRIBUTE;

/**
 游戏过程中，开启直播

 @param livingId 直播唯一标识，必填，不能为NULL
 @param pushStreamUrl 第三方推流地址，必填，不能为NULL
 @param success 调用开启直播成功
 @param fail 调用开启直播失败
 @return 是否开始开启直播
 */
- (BOOL) startLivingWithLivingId:(NSString *)livingId pushStreamUrl:(NSString *)pushStreamUrl Success:(void(^)(BOOL success))success Fail:(void(^)(NSString *errorCode, NSString *errorMsg))fail;

/**
 游戏过程中，关闭直播

 @param livingId 直播间ID，必填，不能为NULL
 @return 是否开始结束直播
 */
- (BOOL) stopLivingWithLivingId:(NSString *)livingId;

/**
 获取授权码，控制端同意转让控制权后调用
 @return 是否开始获取授权码
 */
- (BOOL) getAuthCode;

/**
获取控制权，申请端调用

@param cid 控制端cid，必填，不能为NULL
@param authCode 授权码，必填，不能为NULL
@param enabled 是否获取帧渲染回调数据
@return 是否开始获取控制权
**/
- (BOOL) gainControlWithMasterCid:(NSString *)cid authCode:(NSString *)authCode frameRenderCallback:(BOOL)enabled;

- (BOOL) gainControlWithMasterCid:(NSString *)cid authCode:(NSString *)authCode;

/**
 查询是否支持直播的方法，建议APP调用时机为收到第一帧回调之后

 @return 是否支持直播枚举结果
 */
- (ELivingCapabilityStatus) getLivingCapabilityStatus;

/**
 获取debug延迟信息
 @return 延迟信息
 */
- (NSString *)getDebugDelayInfo;

/**
 心跳超时测试
 @param show 是否开启弹窗提示
 */
- (void)heartBeatPongTimeout:(BOOL)show;

/**
 获取推流域名
 @return 推流域名
 */
- (NSString *)getStreamingDomain;

- (BOOL) sendWsMessage:(HMCloudPlayerWSMessage *)msg;
- (BOOL) sendWsMessage:(HMCloudPlayerWSMessage *)msg callback:(HMCPWSMessageBlock)callback;
- (BOOL) sendWSText:(CloudWSManagerProto)proto text:(NSString *)text;
- (BOOL) sendWSData:(CloudWSManagerProto)proto data:(NSData *)data;
/**
 当客户端app调用play方法长时间无反应，导致app请求超时，可调用此方法获取sdk运行状态
 */
- (CloudPlayerTimeoutStatus) getCloudPlayerTimeoutStatus;

/**
 获取实例类型
 @return 实例类型
 */
- (CloudInstanceType)getCloudInstanceType;

typedef void (^HMAssignControlCallback)(NSArray<HMCloudPlayerControlInfo *>*);

/**
 x86分配控制权
 @param controlInfos 控制权model
 @param success 分配控制权成功
 @param fail 分配控制权失败
 */
- (void)assignGameControlWithControlInfos:(NSArray<HMCloudPlayerControlInfo *>*)controlInfos Success:(HMAssignControlCallback)success Fail:(void (^)(NSString *errorCode, NSString *errorMsg))fail;

/**
 x86查询控制权
 @param cid cid
 @param success 查询控制权成功
 @pramm fail    查询控制权失败
 */
- (void)queryGameControlWithCid:(NSString *)cid Success:(HMAssignControlCallback)success Fail:(void (^)(NSString *errorCode, NSString *errorMsg))fail;
/**
 开始下载图片
 @param downloadList 下载列表
 @param cloudFileDownloadResponseBlock 下载某一个文件回调
 @param cloudFileDownloadComplete 下载完成回调
 @return 是否成功调用下载方法
 */

- (BOOL)startDownload:(NSArray<HMFile *> *)downloadList cloudFileDownloadResponseBlock:(HMCloudFileDownloadResponseBlock)cloudFileDownloadResponseBlock cloudFileDownloadComplete:(HMCloudFileDownloadComplete)cloudFileDownloadComplete;

/**
 取消下载图片
 @param cancelList 取消列表
 @param cloudFileCancelDownloadResponseBlock 取消某一个文件回调
 @param cloudFileCancelDownloadComplete 取消完成回调
 @return 是否成功调用取消下载方法
 */

- (BOOL)startCancelDownload:(NSArray <HMFile *>*)cancelList
cloudFileCancelDownloadResponseBlock:(HMCloudFileCancelDownloadResponseBlock)cloudFileCancelDownloadResponseBlock
cloudFileCancelDownloadComplete:(HMCloudFileCancelDownloadComplete)cloudFileCancelDownloadComplete;

@end


@protocol HMCloudPlayerDelegate <NSObject>

- (void) cloudPlayerSceneChangedCallback:(NSDictionary *)dict;
- (void) cloudPlayerTouchBegan;
- (void) cloudPlayerUsageAuthorization:(HMCloudPlayerUsageAuthorization)type success:(void (^)(BOOL authorization))success;
- (void) cloudPlayerDidReceiveWSMessage:(HMCloudPlayerWSMessage *)msg;
- (void) cloudPlayerKeyboardStatusChanged:(CloudPlayerKeyboardStatus)status;
- (void) cloudPlayerWSManagerDidConnect:(CloudWSManagerProto)proto;
- (void) cloudPlayerWSManagerDidDisconnect:(CloudWSManagerProto)proto error:(NSError*)error;
- (void) cloudPlayerWSManager:(CloudWSManagerProto)proto didReceiveMessage:(NSString *)message;
- (void) cloudPlayerWSManager:(CloudWSManagerProto)proto didReceiveData:(NSData *)data;
- (void) cloudPlayerConnectionIpChanged:(NSDictionary *)dict;
- (void) cloudPlayerScreenCap:(NSDictionary *)dict;
- (void) cloudPlayerShared:(CloudPlayerShareType)shareType dataDict:(NSDictionary *)dataDict;
@end
