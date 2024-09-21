//
//  KeyDetailModel.h
//  hm_cloud
//
//  Created by a水 on 2024/9/14.
//

#import <Foundation/Foundation.h>
#import "KeyModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    TypeJoystick = 1,
    TypeKeyboard,
} Type;

@interface KeyDetailModel : NSObject

@property (nonatomic, assign) BOOL isOfficial;

@property (nonatomic, assign)  BOOL use;
@property (nonatomic, assign)  Type type;
@property (nonatomic, strong)  NSNumber *createTime;
@property (nonatomic, strong)  NSString *name;
@property (nonatomic, strong)  NSString *game_id;
@property (nonatomic, strong)  NSString *user_id;
@property (nonatomic, strong)  NSString *ID;
@property (nonatomic, strong)  NSArray<KeyModel *> *keyboard;

@end

NS_ASSUME_NONNULL_END
