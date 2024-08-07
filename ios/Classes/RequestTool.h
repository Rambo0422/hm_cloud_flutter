//
//  RequestTool.h
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

#define k_api_host           @"https://api-cgfc.3ayx.net"

/// 获取云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
#define k_api_getKeyboard    @"/api/cloudgame/keyboard/get"

/// 更新云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
/// keyboard 虚拟键盘集合
#define k_api_updateKeyboard @"/api/cloudgame/keyboard/update"

/// 还原云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
#define k_api_resetKeyboard  @"/api/cloudgame/keyboard/reset"

typedef enum : NSUInteger {
    Request_POST,
    Request_GET,
    Request_PUT
} RequestMethodType;

@interface RequestTool : NSObject

+ (RequestTool *)share;

- (void)requestUrl:(NSString *)uri methodType:(RequestMethodType)methodType params:(NSDictionary *_Nullable)param faildCallBack:(nullable void (^)(void))faildCallBack successCallBack:(nullable void (^)(id obj))successCallBack;

@end

NS_ASSUME_NONNULL_END
