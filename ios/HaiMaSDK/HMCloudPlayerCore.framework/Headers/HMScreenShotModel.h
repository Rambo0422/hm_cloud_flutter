//
//  HMScreenShotModel.h
//  HMCloudPlayerCore
//
//  Created by apple on 2021/10/19.
//  Copyright © 2021 Apple. All rights reserved.
//

#import "HMCCBaseModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface HMScreenShotModel : HMCCBaseModel
@property(nonatomic,copy)      NSString *cid;           //必填 cid
@property(nonatomic,assign)    NSInteger opType;        //必填 1-截图，2-开始周期性截图，3-停止周期性截图
@property(nonatomic,copy)      NSString *txId;          //必填 事务ID,每次调用传入的值保持唯一性, 消息重发时该字段维持不变
@property(nonatomic,copy)      NSString *interval;      //必填 指令间隔 单位毫秒，默认1000，最小1000毫秒，最大60000毫秒，代表间隔n毫秒执行一次截图。 当opType等于2时该字段有效
@property(nonatomic,copy)      NSString *format;        //选填 图片格式，如png
@property(nonatomic,copy)      NSString *size;          //必填 单张图片大小，单位MB, 默认1 当opType等于1或2该字段有效
@property(nonatomic,copy)      NSString *pixel;         //必填 分辨率，格式宽x高, 默认1920x1080 当opType等于1或2该字段有效
@property(nonatomic,copy)      NSString *uploadType;    //选填 图片上传协议类型,缺省为:http
@property(nonatomic,copy)      NSString *uploadUrl;     //必填 图片上传地址, 由租户业务侧指定，必须是post请求
@end

NS_ASSUME_NONNULL_END
