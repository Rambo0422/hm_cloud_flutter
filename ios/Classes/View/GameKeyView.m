//
//  GameKeyView.m
//  AFNetworking
//
//  Created by a水 on 2024/8/7.
//


#import "CrossView.h"
#import "GameButton.h"
#import "GameKeyView.h"
#import "HmCloudTool.h"
#import "JoystickView.h"
#import "SanA_Macro.h"

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
                    NSDictionary *xDict = @{
                            @"inputState": @1,
                            @"inputOp": @(1027),
                            @"value": @((int)roundf(point.x * 32767))
                    };

                    NSDictionary *yDict = @{
                            @"inputState": @1,
                            @"inputOp": @(1028),
                            @"value": @((int)roundf(-point.y * 32767))
                    };
                    [[HmCloudTool share] sendCustomKey:@[xDict, yDict]];
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
                    NSDictionary *xDict = @{
                            @"inputState": @1,
                            @"inputOp": @(1029),
                            @"value": @((int)roundf(point.x * 32767))
                    };

                    NSDictionary *yDict = @{
                            @"inputState": @1,
                            @"inputOp": @(1030),
                            @"value": @((int)roundf(-point.y * 32767))
                    };

                    [[HmCloudTool share] sendCustomKey:@[xDict, yDict]];
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
                    NSDictionary *crossDict = @{
                            @"inputState": @1,
                            @"inputOp": @(1024),
                            @"value": op,
                    };
                    [[HmCloudTool share] sendCustomKey:@[crossDict]];
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

    btn.upCallback = ^(NSArray<NSDictionary *> *keyList) {
        [[HmCloudTool share] sendCustomKey:keyList];
    };

    btn.downCallback = ^(NSArray<NSDictionary *> *keyList) {
        [[HmCloudTool share] sendCustomKey:keyList];
    };

    [self addSubview:btn];

    [btn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(@(m.top));
        make.left.equalTo(@(m.left));
        make.size.mas_equalTo(CGSizeMake(m.width, m.height));
    }];
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];

    if (hitView == self) {
        return nil;
    }

    return hitView;
}

@end
