//
//  GameKeyView.m
//  AFNetworking
//
//  Created by a水 on 2024/8/7.
//


#import "CrossView.h"
#import "GameButtonView.h"
#import "GameKeyView.h"
#import "HmCloudTool.h"
#import "JoystickArrowView.h"
#import "JoystickView.h"
#import "SanA_Macro.h"

@interface GameKeyView () {
    BOOL _isEdit;
}

@end

@implementation GameKeyView

- (instancetype)initWithEdit:(BOOL)isEdit
{
    self = [super init];

    if (self) {
        _isEdit = isEdit;
    }

    return self;
}

- (void)clear {
    while (self.subviews.count > 0) [self.subviews.lastObject removeFromSuperview];
}

- (void)setKeyList:(NSArray<KeyModel *> *)keyList {
    _keyList = keyList;

    if (keyList.count) {
        while (self.subviews.count > 0) [self.subviews.lastObject removeFromSuperview];
    }

    for (KeyModel *m in keyList) {
        switch (m.key_type) {
            case KEY_kb_rock_letter:{
                JoystickArrowView *joy = [[JoystickArrowView alloc] initWithEidt:_isEdit];
                joy.bgImg = k_BundleImage(@"key_joy_wasd");
                joy.thumbImg = k_BundleImage(@"key_joy_thumb_normal");
                joy.callback = ^(Direction oldD, Direction newD) {
                    [self configCustomKeyList:oldD new:newD isArrow:NO];
                };
                joy.model = m;
                joy.tapCallback = self.tapCallback;
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_rock_arrow:{
                JoystickArrowView *joy = [[JoystickArrowView alloc] initWithEidt:_isEdit];
                joy.bgImg = k_BundleImage(@"key_joy_arrow");
                joy.thumbImg = k_BundleImage(@"key_joy_thumb_normal");
                joy.model = m;
                joy.callback = ^(Direction oldD, Direction newD) {
                    [self configCustomKeyList:oldD new:newD isArrow:YES];
                };
                joy.tapCallback = self.tapCallback;
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_xobx_rock_lt:{
                JoystickView *joy = [[JoystickView alloc] initWithEidt:_isEdit];
                joy.bgImg = k_BundleImage(@"key_xbox_rock");
                joy.thumbImg = k_BundleImage(@"key_xbox_rock_thumb_l");
                joy.model = m;
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
                joy.tapCallback = self.tapCallback;
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_xobx_rock_rt:{
                JoystickView *joy = [[JoystickView alloc] initWithEidt:_isEdit];
                joy.bgImg = k_BundleImage(@"key_xbox_rock");
                joy.thumbImg = k_BundleImage(@"key_xbox_rock_thumb_r");
                joy.model = m;
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
                joy.tapCallback = self.tapCallback;
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_xobx_cross:{
                CrossView *cross = [[CrossView alloc] initWithEidt:_isEdit];
                cross.model = m;
                cross.callback = ^(NSNumber *_Nonnull op) {
                    NSDictionary *crossDict = @{
                            @"inputState": @1,
                            @"inputOp": @(1024),
                            @"value": op,
                    };
                    [[HmCloudTool share] sendCustomKey:@[crossDict]];
                };
                cross.tapCallback = self.tapCallback;
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
    GameButtonView *btn = [[GameButtonView alloc] initWithEidt:_isEdit];

    btn.model = m;

    btn.tapCallback = self.tapCallback;

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

- (void)configCustomKeyList:(Direction)oldD new:(Direction)newD isArrow:(BOOL)isArrow {
    // w = 87, a = 65, s = 83, d = 68
    // ↑ = 38, ← = 37, ↓ = 40, → = 39

    NSInteger top = isArrow ? 38 : 87;
    NSInteger left = isArrow ? 37 : 65;
    NSInteger bottom = isArrow ? 40 : 83;
    NSInteger right = isArrow ? 39 : 68;

    NSMutableArray<NSDictionary *> *dictArr = [NSMutableArray array];

    if (oldD != DirectionNormal) {
        /// 如果old不是默认值，则要发一个old的抬起
        /// w，a，s，d，wa，wd，sa，sd
        /// ↑，↓，←，→，↖，↗，↙，↘
        switch (oldD) {
            case DirectionTop:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(top),
                     @"value": @(0)
                }];
            }



            break;

            case DirectionTopRight:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(top),
                     @"value": @(0)
                }];

                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(right),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionRight:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(right),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionBottomRight:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(right),
                     @"value": @(0)
                }];

                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(bottom),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionBottom:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(bottom),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionBottomLeft:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(bottom),
                     @"value": @(0)
                }];
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(left),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionLeft:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(left),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionTopLeft:{
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(left),
                     @"value": @(0)
                }];
                [dictArr addObject:@{
                     @"inputState": @3,
                     @"inputOp": @(top),
                     @"value": @(0)
                }];
            }

            break;

            default:
                break;
        }
    }

    if (newD != DirectionNormal) {
        /// 如果new不是默认值，则要发一个new的按下
        /// w，a，s，d，wa，wd，sa，sd
        /// ↑，↓，←，→，↖，↗，↙，↘
        switch (newD) {
            case DirectionTop:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(top),
                     @"value": @(0)
                }];
            }



            break;

            case DirectionTopRight:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(top),
                     @"value": @(0)
                }];

                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(right),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionRight:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(right),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionBottomRight:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(right),
                     @"value": @(0)
                }];

                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(bottom),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionBottom:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(bottom),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionBottomLeft:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(bottom),
                     @"value": @(0)
                }];
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(left),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionLeft:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(left),
                     @"value": @(0)
                }];
            }

            break;

            case DirectionTopLeft:{
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(left),
                     @"value": @(0)
                }];
                [dictArr addObject:@{
                     @"inputState": @2,
                     @"inputOp": @(top),
                     @"value": @(0)
                }];
            }

            break;

            default:
                break;
        }
    }

    [[HmCloudTool share] sendCustomKey:dictArr];
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];

    if (hitView == self) {
        return nil;
    }

    return hitView;
}

@end
