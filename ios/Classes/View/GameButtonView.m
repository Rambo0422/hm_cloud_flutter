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

- (instancetype)initWithEidt:(BOOL)isEdit model:(nonnull KeyModel *)model {
    self = [super initWithEidt:isEdit model:model];

    if (self) {
        self.btn = [UIButton buttonWithType:UIButtonTypeCustom];
        self.btn.frame = self.bounds;
        self.btn.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self.contentView addSubview:self.btn];

        [self.btn setBackgroundImage:k_BundleImage([self getImg:model isHigh:NO]) forState:UIControlStateNormal];

        [self.btn setBackgroundImage:k_BundleImage([self getImg:model isHigh:YES]) forState:UIControlStateHighlighted];

        [self.btn setBackgroundImage:k_BundleImage([self getImg:model isHigh:YES]) forState:UIControlStateSelected];

        @weakify(self);
        [RACObserve(model, text) subscribeNext:^(id _Nullable x) {
            @strongify(self);

            [self.btn setTitle:model.text
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

    return self;
}

- (void)handlePan:(UIPanGestureRecognizer *)gestureRecognizer {
    UIView *draggedView = gestureRecognizer.view;

    // 获取拖拽的位移
    CGPoint translation = [gestureRecognizer translationInView:draggedView.superview];

    // 根据拖拽的位移更新视图的位置
    CGPoint newCenter = CGPointMake(draggedView.center.x + translation.x, draggedView.center.y + translation.y);

    // 获取屏幕边界
    CGFloat halfWidth = CGRectGetWidth(draggedView.bounds) / 2.0;
    CGFloat halfHeight = CGRectGetHeight(draggedView.bounds) / 2.0;
    CGFloat screenWidth = CGRectGetWidth(draggedView.superview.bounds);
    CGFloat screenHeight = CGRectGetHeight(draggedView.superview.bounds);

    // 确保新中心点不会超出屏幕边界
    newCenter.x = MAX(halfWidth, MIN(screenWidth - halfWidth, newCenter.x));
    newCenter.y = MAX(halfHeight, MIN(screenHeight - halfHeight, newCenter.y));

    // 更新视图的位置
    draggedView.center = newCenter;


    NSInteger top = (NSInteger)CGRectGetMinY(draggedView.frame);
    NSInteger left = (NSInteger)CGRectGetMinX(draggedView.frame);

    //    (_top * (kScreenH / 375.0)) = top;
    //    (_left * (kScreenW / 667.0)) = left;

    self.model.top = top / (kScreenH / 375.0);
    self.model.left = left / (kScreenW / 667.0);


    // 重置拖拽手势的累积位移
    [gestureRecognizer setTranslation:CGPointZero inView:self.superview];

    if (self.tapCallback) {
        self.tapCallback(self.model);
    }
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

- (void)touchUp {
    if (self.model.key_type == KEY_xbox_combination) {
        NSMutableArray *arr = [NSMutableArray array];

        for (KeyModel *m in self.model.composeArr) {
            [arr addObject:[self xboxKeyUp:m]];
        }

        if (self.downCallback) {
            self.downCallback(arr);
        }

        return;
    }

    if (self.model.key_type == KEY_kb_combination) {
        NSMutableArray *arr = [NSMutableArray array];

        for (KeyModel *m in self.model.composeArr) {
            if ([m.type containsString:@"kb-mouse"]) {
                [arr addObject:[self mouseKeyUp:m]];
            } else {
                [arr addObject:[self keyboardKeyUp:m]];
            }
        }

        if (self.downCallback) {
            self.downCallback(arr);
        }

        return;
    }

    NSDictionary *dict;

    if ([self.model.type containsString:@"xbox-"]) {
        dict = [self xboxKeyUp:self.model];
    } else if ([self.model.type containsString:@"kb-mouse"]) {
        // 鼠标 按键

        dict = [self mouseKeyUp:self.model];
    } else {
        dict = [self keyboardKeyUp:self.model];
    }

    if (self.upCallback) {
        self.upCallback(@[dict]);
    }
}

/// MARK: 手指按下
- (void)didTapTouchDown {
    if (self.model.click == 1) {
        return;
    }

    [self touchDown];
}

- (void)touchDown {
    // 触发中等震动
    if ([HmCloudTool share].isVibration) {
        UIImpactFeedbackGenerator *mediumImpact = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleMedium];
        [mediumImpact impactOccurred];
    }

    if (self.model.key_type == KEY_xbox_combination) {
        NSMutableArray *arr = [NSMutableArray array];

        for (KeyModel *m in self.model.composeArr) {
            [arr addObject:[self xboxKeyDown:m]];
        }

        if (self.downCallback) {
            self.downCallback(arr);
        }

        return;
    }

    if (self.model.key_type == KEY_kb_combination) {
        NSMutableArray *arr = [NSMutableArray array];

        for (KeyModel *m in self.model.composeArr) {
            if ([m.type containsString:@"kb-mouse"]) {
                [arr addObject:[self mouseKeyDown:m]];
            } else {
                [arr addObject:[self keyboardKeyDown:m]];
            }
        }

        if (self.downCallback) {
            self.downCallback(arr);
        }

        return;
    }

    NSDictionary *dict;

    if ([self.model.type containsString:@"xbox-"]) {
        dict = [self xboxKeyDown:self.model];
    } else if ([self.model.type containsString:@"kb-mouse"]) {
        dict = [self mouseKeyDown:self.model];
    } else {
        dict = [self keyboardKeyDown:self.model];
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
        case KEY_kb_combination:
        case KEY_xbox_combination:

            return isHigh ? @"key_kb_round_h" : @"key_kb_round_n";

        case KEY_kb_xbox_square:

            return isHigh ? @"key_kb_square_h" : @"key_kb_square_n";

        case KEY_kb_xbox_round_medium:

            return isHigh ? @"key_kb_round_h" : @"key_kb_round_n";

        case KEY_kb_xbox_round_small:

            return isHigh ? @"key_kb_round_h" : @"key_kb_round_n";

        case KEY_kb_xbox_elliptic:

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
