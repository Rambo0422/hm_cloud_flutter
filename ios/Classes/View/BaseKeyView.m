//
//  BaseKeyView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "BaseKeyView.h"
#import "SanA_Macro.h"

@implementation BaseKeyView


- (instancetype)initWithEidt:(BOOL)isEdit model:(KeyModel *)model
{
    self = [super init];

    if (self) {
        self.isEdit = isEdit;
        self.model = model;
        self.contentView = [[UIView alloc] init];
        self.contentView.frame = self.bounds;
        self.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self addSubview:self.contentView];


//        self.hidden = self.model.isRou;
        self.frame = CGRectMake(self.model.left, self.model.top, self.model.width, self.model.height);

        if (self.isEdit) {
            self.contentView.userInteractionEnabled = NO;
            UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tap:)];
            tap.delaysTouchesBegan = YES;
            tap.delaysTouchesEnded = YES;
            [self addGestureRecognizer:tap];
            
            
            self.backgroundColor = [[UIColor whiteColor] colorWithAlphaComponent:0.3];
            
        }

        @weakify(self);
        [RACObserve(self.model, zoom) subscribeNext:^(id _Nullable x) {
            @strongify(self);
            self.bounds = CGRectMake(0, 0, self.model.width, self.model.height);
        }];

        [RACObserve(self.model, opacity) subscribeNext:^(id _Nullable x) {
            @strongify(self);
            self.contentView.alpha = self.model.opacity / 100.0;
        }];
    }

    return self;
}

- (void)tap:(UITapGestureRecognizer *)tap {
    if (self.tapCallback) {
        self.backgroundColor = [kColor(0xC6EC4B) colorWithAlphaComponent:0.6];
        self.tapCallback(self.model,self);
    }
}

// MARK: xbox 抬起
- (NSDictionary *)xboxKeyUp:(KeyModel *)m {
    NSDictionary *dict;

    // xbox 按键
    if (m.inputOp == 1025 || m.inputOp == 1026) {
        // LT RT 特殊按键
        dict = @{
                @"inputState": @1,
                @"inputOp": @(m.inputOp),
                @"value": @0
        };
    } else {
        NSInteger value = 0;

        [[HmCloudTool share].xboxKeyList removeObject:@(m.inputOp)];

        for (NSNumber *n in [HmCloudTool share].xboxKeyList) {
            value = value | n.integerValue;
        }

        // 普通按键
        dict = @{
                @"inputState": @1,
                @"inputOp": @1024,
                @"value": @(value)
        };
    }

    return dict;
}

// MARK: 鼠标 抬起
- (NSDictionary *)mouseKeyUp:(KeyModel *)m {
    NSDictionary *dict;

    // 鼠标 按键
    if (m.key_type == KEY_mouse_wheel_up || m.key_type == KEY_mouse_wheel_down) {
        // 滚轮滚动
        dict = @{
                @"inputState": @1,
                @"inputOp": @(m.inputOp),
                @"value": @0
        };
    } else {
        // 左键 右键 中键

        dict = @{
                @"inputState": @3,
                @"inputOp": @(m.inputOp),
                @"value": @0
        };
    }

    return dict;
}

// MARK: 键盘 抬起
- (NSDictionary *)keyboardKeyUp:(KeyModel *)m {
    NSDictionary *dict = @{
            @"inputState": @3,
            @"inputOp": @(m.inputOp),
            @"value": @0
    };

    return dict;
}

// MARK: xbox 按下
- (NSDictionary *)xboxKeyDown:(KeyModel *)m {
    NSDictionary *dict;

    // xbox 按键
    if (m.inputOp == 1025 || m.inputOp == 1026) {
        // LT RT 特殊按键
        dict = @{
                @"inputState": @1,
                @"inputOp": @(m.inputOp),
                @"value": @255
        };
    } else {
        // 普通按键

        NSInteger value = m.inputOp;

        for (NSNumber *n in [HmCloudTool share].xboxKeyList) {
            value = value | n.integerValue;
        }

        dict = @{
                @"inputState": @1,
                @"inputOp": @1024,
                @"value": @(value)
        };

        [[HmCloudTool share].xboxKeyList addObject:@(m.inputOp)];
    }

    return dict;
}

// MARK: 鼠标 按下
- (NSDictionary *)mouseKeyDown:(KeyModel *)m {
    NSDictionary *dict;

    // 鼠标 按键
    if (m.key_type == KEY_mouse_wheel_up || m.key_type == KEY_mouse_wheel_down) {
        // 滚轮滚动
        dict = @{
                @"inputState": @1,
                @"inputOp": @(m.inputOp),
                @"value": (m.key_type == KEY_mouse_wheel_up) ? @1 : @-1
        };
    } else {
        // 左键 右键 中键

        dict = @{
                @"inputState": @2,
                @"inputOp": @(m.inputOp),
                @"value": @0
        };
    }

    return dict;
}

// MARK: 键盘 按下
- (NSDictionary *)keyboardKeyDown:(KeyModel *)m {
    NSDictionary *dict = @{
            @"inputState": @2,
            @"inputOp": @(m.inputOp),
            @"value": @0
    };

    return dict;
}

@end
