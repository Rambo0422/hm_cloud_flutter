//
//  GameButton.m
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import "GameButtonView.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"

@interface GameButtonView ()

@property (nonatomic, strong) UIButton *btn;

@end

@implementation GameButtonView

- (instancetype)initWithEidt:(BOOL)isEdit {
    self = [super initWithEidt:isEdit];

    if (self) {
        self.btn = [UIButton buttonWithType:UIButtonTypeCustom];
        self.btn.frame = self.bounds;
        self.btn.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        self.isEdit = isEdit;
        [self.contentView addSubview:self.btn];
    }

    return self;
}

- (void)setModel:(KeyModel *)m {
    [super setModel:m];
    [self.btn setBackgroundImage:k_BundleImage([self getImg:m isHigh:NO]) forState:UIControlStateNormal];

    [self.btn setBackgroundImage:k_BundleImage([self getImg:m isHigh:YES]) forState:UIControlStateHighlighted];

    [self.btn setBackgroundImage:k_BundleImage([self getImg:m isHigh:YES]) forState:UIControlStateSelected];

    @weakify(self);
    [RACObserve(m, text) subscribeNext:^(id _Nullable x) {
        @strongify(self);

        [self.btn setTitle:m.text
                  forState:UIControlStateNormal];
    }];


    [self.btn setTitleColor:[kColor(0xFFFFFF) colorWithAlphaComponent:0.6] forState:UIControlStateNormal];
    self.btn.titleLabel.font = [UIFont systemFontOfSize:9];

    if (self.isEdit) {
        UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
        [self addGestureRecognizer:pan];
    } else {
        [self.btn addTarget:self action:@selector(didTapTouchUp) forControlEvents:UIControlEventTouchUpInside];
        [self.btn addTarget:self action:@selector(didTapTouchDown) forControlEvents:UIControlEventTouchDown];
    }
}

- (void)handlePan:(UIPanGestureRecognizer *)gestureRecognizer {
    // 获取拖拽的位移
    CGPoint translation = [gestureRecognizer translationInView:self.superview];

    // 根据拖拽的位移更新视图的位置
    self.center = CGPointMake(self.center.x + translation.x, self.center.y + translation.y);

    // 重置拖拽手势的累积位移
    [gestureRecognizer setTranslation:CGPointZero inView:self.superview];

    // 如果手势已经结束，可能需要处理其他逻辑
    if (gestureRecognizer.state == UIGestureRecognizerStateEnded) {
        // 可以在这里添加手势结束后的处理逻辑，例如检测视图是否拖出了屏幕
        // 或者实现回弹动画等
    }

    return;
}

/// MARK: 手指抬起
- (void)didTapTouchUp {
    if (self.model.click == 1) {
        if (self.btn.isSelected) {
            [self touchUp];
        } else {
            [self touchDown];
        }

        self.btn.selected = !self.btn.selected;
    } else {
        [self touchUp];
    }
}

/// MARK: 手指按下
- (void)didTapTouchDown {
    if (self.model.click == 1) {
        return;
    }

    [self touchDown];
}

- (void)touchUp {
    NSDictionary *dict;

    if ([self.model.type containsString:@"xbox-"]) {
        // xbox 按键
        if (self.model.inputOp == 1025 || self.model.inputOp == 1026) {
            // LT RT 特殊按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.model.inputOp),
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
    } else if ([self.model.type containsString:@"kb-mouse"]) {
        // 鼠标 按键

        if (self.model.key_type == KEY_mouse_wheel_up || self.model.key_type == KEY_mouse_wheel_down) {
            // 滚轮滚动
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.model.inputOp),
                    @"value": @0
            };
        } else {
            // 左键 右键 中键

            dict = @{
                    @"inputState": @3,
                    @"inputOp": @(self.model.inputOp),
                    @"value": @0
            };
        }
    } else {
        dict = @{
                @"inputState": @3,
                @"inputOp": @(self.model.inputOp),
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

    if ([self.model.type containsString:@"xbox-"]) {
        // xbox 按键
        if (self.model.inputOp == 1025 || self.model.inputOp == 1026) {
            // LT RT 特殊按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.model.inputOp),
                    @"value": @255
            };
        } else {
            // 普通按键
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @1024,
                    @"value": @(self.model.inputOp)
            };
        }
    } else if ([self.model.type containsString:@"kb-mouse"]) {
        // 鼠标 按键

        if (self.model.key_type == KEY_mouse_wheel_up || self.model.key_type == KEY_mouse_wheel_down) {
            // 滚轮滚动
            dict = @{
                    @"inputState": @1,
                    @"inputOp": @(self.model.inputOp),
                    @"value": (self.model.key_type == KEY_mouse_wheel_up) ? @1 : @-1
            };
        } else {
            // 左键 右键 中键

            dict = @{
                    @"inputState": @2,
                    @"inputOp": @(self.model.inputOp),
                    @"value": @0
            };
        }
    } else {
        dict = @{
                @"inputState": @2,
                @"inputOp": @(self.model.inputOp),
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
