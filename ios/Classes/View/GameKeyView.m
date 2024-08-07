//
//  GameKeyView.m
//  AFNetworking
//
//  Created by a水 on 2024/8/7.
//


#import "CrossView.h"
#import "GameKeyView.h"
#import "JoystickView.h"
#import "SanA_Macro.h"

@interface GameButton : UIButton

@property (nonatomic, strong) KeyModel *m;

@end

@implementation GameButton

@end

@implementation GameKeyView

/*
   // Only override drawRect: if you perform custom drawing.
   // An empty implementation adversely affects performance during animation.
   - (void)drawRect:(CGRect)rect {
    // Drawing code
   }
 */

- (void)setKeyList:(NSArray<KeyModel *> *)keyList {
    _keyList = keyList;

    if (keyList.count) {
        while (self.subviews.count > 0) [self.subviews.lastObject removeFromSuperview];
    }

    for (KeyModel *m in keyList) {
        switch (m.key_type) {
            case KEY_kb_rock_letter:{
            }
            break;

            case KEY_kb_rock_arrow:{
            }
            break;

            case KEY_kb_xobx_rock_lt:{
                JoystickView *joy = [[JoystickView alloc] init];
                joy.bgImg = k_BundleImage(@"key_xbox_rock");
                joy.thumbImg = k_BundleImage(@"key_xbox_rock_thumb_l");
                joy.callback = ^(CGPoint point) {
                };
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_xobx_rock_rt:{
                JoystickView *joy = [[JoystickView alloc] init];
                joy.bgImg = k_BundleImage(@"key_xbox_rock");
                joy.thumbImg = k_BundleImage(@"key_xbox_rock_thumb_r");
                joy.callback = ^(CGPoint point) {
                };
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_xobx_cross:{
                CrossView *cross = [[CrossView alloc] init];
                cross.callback = ^(NSNumber *_Nonnull op) {
                };
                [self addSubview:cross];
                [cross mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_unknown:{
            }
            break;

            default:{
                [self initKey:m];
            }
            break;
        }
    }
}

- (void)initKey:(KeyModel *)m {
    GameButton *btn = [GameButton buttonWithType:UIButtonTypeCustom];

    btn.m = m;
    [btn setBackgroundImage:k_BundleImage([self getImg:m isHigh:NO]) forState:UIControlStateNormal];

    [btn setBackgroundImage:k_BundleImage([self getImg:m isHigh:YES]) forState:UIControlStateHighlighted];

    [btn setTitle:m.text forState:UIControlStateNormal];
    [btn setTitleColor:[kColor(0xFFFFFF) colorWithAlphaComponent:0.6] forState:UIControlStateNormal];
    btn.titleLabel.font = [UIFont systemFontOfSize:9];

    [self addSubview:btn];

    [btn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(@(m.top));
        make.left.equalTo(@(m.left));
        make.size.mas_equalTo(CGSizeMake(m.width, m.height));
    }];
}

- (NSString *)getImg:(KeyModel *)model isHigh:(BOOL)isHigh {
    switch (model.key_type) {
        case KEY_mouse_left:

            return isHigh ? @"key_ms_left_h" : @"key_ms_left_n";

        case KEY_mouse_right:

            return isHigh ? @"key_ms_right_h" : @"key_ms_right_n";

        case KEY_mouse_wheel_center:

            return isHigh ? @"key_ms_center_h" : @"key_ms_center_n";

        case KEY_mouse_wheel_up:

            return isHigh ? @"key_ms_up_h" : @"key_ms_up_n";

        case KEY_mouse_wheel_down:

            return isHigh ? @"key_ms_down_h" : @"key_ms_down_n";

        case KEY_kb_round:

            return isHigh ? @"key_kb_round_h" : @"key_kb_round_n";

        case KEY_kb_xobx_square:

            return isHigh ? @"key_kb_square_h" : @"key_kb_square_n";

        case KEY_kb_xobx_round_medium:

            return isHigh ? @"key_kb_round_h" : @"key_kb_round_n";

        case KEY_kb_xobx_round_small:

            return isHigh ? @"key_kb_round_h" : @"key_kb_round_n";

        case KEY_kb_xobx_elliptic:

            if (model.inputOp == 16) {
                return isHigh ? @"key_xbox_set_h" : @"key_xbox_set_n";
            } else {
                return isHigh ? @"key_xbox_menu_h" : @"key_xbox_menu_n";
            }

        default:
            return @"";
    }
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];

    if (hitView == self) {
        return nil;
    }

    return hitView;
}

@end
