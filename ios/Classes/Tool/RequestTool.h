//
//  RequestTool.h
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

#define k_api_host                     @"https://api-cgfc.3ayx.net"

/// 获取云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
#define k_api_getKeyboard              @"/api/cloudgame/keyboard/get"


/// 获取云游戏虚拟键盘（官方）
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
#define k_api_getKeyboard_v2           @"/api/cloudgame/v2/keyboard/getdefault"

/// 获取云游戏虚拟键盘（用户自定义）
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
/// page 分页
/// size 单页数量
#define k_api_getKeyboard_custom_v2    @"/api/cloudgame/v2/keyboard/get"


///  新增云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
/// keyboard 虚拟按键集合
#define k_api_createKeyboard_custom_v2 @"/api/cloudgame/v2/keyboard/create"

///  删除云游戏虚拟键盘
/// params：
/// id 虚拟键盘id
#define k_api_deleteKeyboard_custom_v2 @"/api/cloudgame/v2/keyboard/del"


///  更新云游戏虚拟键盘
/// params：
/// id 虚拟键盘id
/// use 是否使用 0 ， 1
/// keyboard 虚拟按键集合
#define k_api_updateKeyboard_custom_v2 @"/api/cloudgame/v2/keyboard/update"


/// 更新云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
/// keyboard 虚拟键盘集合
#define k_api_updateKeyboard           @"/api/cloudgame/keyboard/update"

/// 还原云游戏虚拟键盘
/// params：
/// type 1手柄，2键盘
/// game_id 游戏id
#define k_api_resetKeyboard            @"/api/cloudgame/keyboard/reset"

/// 获取游戏详情
///  params:
///  game_id 游戏id
#define k_api_get_game_detail          @"/api/game/details/get"

/// 创建直播间
///  params:
///  cid 海马cid
///  hide 1=隐藏，2=不隐藏
#define k_api_create_liveRoom          @"/api/room/playroom/create"

/// 修改直播间
///  params:
///  cid 海马cid
///  hide 1=隐藏，2=不隐藏
#define k_api_update_liveRoom          @"/api/room/playroom/update"


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
