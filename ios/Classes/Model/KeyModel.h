//
//  KeyModel.h
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    KEY_unknown,

    /// 鼠标 （左键，右键，滚轮点击，滚轮↑，滚轮↓）
    KEY_mouse_left,         // "kb-mouse-lt"
    KEY_mouse_right,        // "kb-mouse-rt"
    KEY_mouse_wheel_center, // kb-mouse-md
    KEY_mouse_wheel_up,     // "kb-mouse-up"
    KEY_mouse_wheel_down,   // "kb-mouse-down"

    /// 键盘
    /// 字母(wasd)摇杆
    /// "kb-rock-letter"
    KEY_kb_rock_letter,


    /// 方向(↑↓←→)摇杆
    /// "kb-rock-arrow"
    KEY_kb_rock_arrow,


    /// 普通圆形按键
    /// "kb-round"
    KEY_kb_round,

    /// xbox
    /// 方形按键 eg. LT(L1) RT(R1) LB(L2) RB(L2)
    /// "xbox-square"
    KEY_kb_xbox_square,


    /// 中等圆形按钮 eg. A B X Y
    /// "xbox-round-medium"
    KEY_kb_xbox_round_medium,


    /// 小圆形按钮 eg. rs ls
    /// "xbox-round-small"
    KEY_kb_xbox_round_small,


    /// 椭圆按钮 eg. 菜单 设置
    ///xbox-elliptic
    KEY_kb_xbox_elliptic,


    /// 左摇杆
    ///"xbox-rock-lt"
    KEY_kb_xbox_rock_lt,


    /// 右摇杆
    ///"xbox-rock-rt"
    KEY_kb_xbox_rock_rt,

    /// 十字键
    /// "xbox-cross"
    KEY_kb_xbox_cross,

    /// kb组合键
    /// "kb-combination"
    KEY_kb_combination,

    /// xbox组合键
    /// "xbox-combination"
    KEY_xbox_combination,

    /// 轮盘键
    /// "kb-roulette"
    KEY_kb_roulette,

    /// 收纳键
    /// "kb_container"
    KEY_kb_container,

    /// 射击键
    /// "kb_shoot"
    KEY_kb_shoot,
} KeyType;



@interface KeyModel : NSObject

@property (nonatomic, assign)  NSInteger top;
@property (nonatomic, assign)  NSInteger left;
@property (nonatomic, assign)  NSInteger height;
@property (nonatomic, assign)  NSInteger width;
@property (nonatomic, assign)  NSInteger zoom;
@property (nonatomic, assign)  NSInteger opacity;
@property (nonatomic, assign)  NSInteger click;
@property (nonatomic, assign)  NSInteger inputOp;
@property (nonatomic, strong)  NSString *text;
@property (nonatomic, strong)  NSString *type;
@property (nonatomic, assign)  NSInteger editIndex;
@property (nonatomic, strong)  NSArray<KeyModel *> *composeArr;
@property (nonatomic, strong)  NSArray<KeyModel *> *rouArr;
@property (nonatomic, strong)  NSArray<KeyModel *> *containerArr;


@property (nonatomic, assign)  KeyType key_type;

- (NSDictionary *)toJson;

@end

NS_ASSUME_NONNULL_END
