//
//  GameButton.m
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import "GameKey_ButtonView.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"

@interface GameKey_ButtonView ()

@property (nonatomic, strong) UIButton *btn;

@property (nonatomic, strong) UILabel *lab;

@end

@implementation GameKey_ButtonView

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

        if (!self.isEdit) {
            [self.btn addTarget:self action:@selector(didTapTouchUp) forControlEvents:UIControlEventTouchUpInside];
            [self.btn addTarget:self action:@selector(didTapTouchDown) forControlEvents:UIControlEventTouchDown];
        }

        NSString *path = [k_SanABundle pathForResource:@"keyboard"
                                                ofType:@"json"];
        NSData *data = [NSData dataWithContentsOfFile:path];
        NSError *error;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data
                                                             options:kNilOptions
                                                               error:&error];

        NSString *key = [NSString stringWithFormat:@"%ld", model.inputOp];

        if ([dict.allKeys containsObject:key]) {
            self.lab = [[UILabel alloc] init];
            self.lab.text = [NSString stringWithFormat:@" %@ ", [dict[key] objectForKey:@"name"]];
            self.lab.backgroundColor = [kColor(0x020202) colorWithAlphaComponent:0.75];
            self.lab.font = [UIFont systemFontOfSize:9 weight:UIFontWeightMedium];
            self.lab.textColor = [kColor(0xC6EC4B) colorWithAlphaComponent:0.75];
            self.lab.layer.cornerRadius = 6;
            self.lab.layer.masksToBounds = YES;
            [self addSubview:self.lab];

            [self.lab mas_makeConstraints:^(MASConstraintMaker *make) {
                make.top.equalTo(@0);
                make.right.equalTo(@0);
                make.height.equalTo(@12);
            }];
        }
    }

    return self;
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
                return isHigh ? @"key_xbox_menu_h" : @"key_xbox_menu_n";
            } else {
                return isHigh ? @"key_xbox_set_h" : @"key_xbox_set_n";
            }

        default:
            return @"";
    }
}

@end
