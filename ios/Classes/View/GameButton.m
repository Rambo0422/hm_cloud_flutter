//
//  GameButton.m
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import "GameButton.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"

@implementation GameButton

- (void)setM:(KeyModel *)m {
    _m = m;
    [self setBackgroundImage:k_BundleImage([self getImg:m isHigh:NO]) forState:UIControlStateNormal];

    [self setBackgroundImage:k_BundleImage([self getImg:m isHigh:YES]) forState:UIControlStateHighlighted];

    [self setBackgroundImage:k_BundleImage([self getImg:m isHigh:YES]) forState:UIControlStateSelected];

    [self setTitle:m.text forState:UIControlStateNormal];
    [self setTitleColor:[kColor(0xFFFFFF) colorWithAlphaComponent:0.6] forState:UIControlStateNormal];
    self.titleLabel.font = [UIFont systemFontOfSize:9];

    [self addTarget:self action:@selector(didTapTouchUp) forControlEvents:UIControlEventTouchUpInside];
    [self addTarget:self action:@selector(didTapTouchDown) forControlEvents:UIControlEventTouchDown];
}

/// MARK: 手指抬起
- (void)didTapTouchUp {
    if (self.m.click == 1) {
        if (self.isSelected) {
            [self touchUp];
        } else {
            [self touchDown];
        }

        self.selected = !self.selected;
    } else {
        [self touchUp];
    }
}

/// MARK: 手指按下
- (void)didTapTouchDown {
    if (self.m.click == 1) {
        return;
    }

    [self touchDown];
}

- (void)touchUp {
    NSDictionary *dict;

    if ([self.m.type containsString:@"xbox-"]) {
        // xbox 按键
        if (self.m.inputOp == 1025 || self.m.inputOp == 1026) {
            // LT RT 特殊按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.m.inputOp),
                    @"value": @0
            };
        } else {
            // 普通按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @1024,
                    @"value": @(0)
            };
        }
    } else if ([self.m.type containsString:@"kb-mouse"]) {
        // 鼠标 按键

        if (self.m.key_type == KEY_mouse_wheel_up || self.m.key_type == KEY_mouse_wheel_down) {
            // 滚轮滚动
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.m.inputOp),
                    @"value": @0
            };
        } else {
            // 左键 右键 中键

            dict = @{
                    @"inputState": @3,
                    @"inputOp": @(self.m.inputOp),
                    @"value": @0
            };
        }
    } else {
        dict = @{
                @"inputState": @3,
                @"inputOp": @(self.m.inputOp),
                @"value": @0
        };
    }

    if (self.upCallback) {
        self.upCallback(@[dict]);
    }
}

- (void)touchDown {
    // 触发中等震动
    if ([HmCloudTool share].isVibration) {
        UIImpactFeedbackGenerator *mediumImpact = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleMedium];
        [mediumImpact impactOccurred];
    }

    NSDictionary *dict;

    if ([self.m.type containsString:@"xbox-"]) {
        // xbox 按键
        if (self.m.inputOp == 1025 || self.m.inputOp == 1026) {
            // LT RT 特殊按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.m.inputOp),
                    @"value": @255
            };
        } else {
            // 普通按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @1024,
                    @"value": @(self.m.inputOp)
            };
        }
    } else if ([self.m.type containsString:@"kb-mouse"]) {
        // 鼠标 按键

        if (self.m.key_type == KEY_mouse_wheel_up || self.m.key_type == KEY_mouse_wheel_down) {
            // 滚轮滚动
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.m.inputOp),
                    @"value": (self.m.key_type == KEY_mouse_wheel_up) ? @1 : @-1
            };
        } else {
            // 左键 右键 中键

            dict = @{
                    @"inputState": @2,
                    @"inputOp": @(self.m.inputOp),
                    @"value": @0
            };
        }
    } else {
        dict = @{
                @"inputState": @2,
                @"inputOp": @(self.m.inputOp),
                @"value": @0
        };
    }

    if (self.downCallback) {
        self.downCallback(@[dict]);
    }
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

@end
