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
#import "JoystickArrowView.h"
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
                JoystickArrowView *joy = [[JoystickArrowView alloc] init];
                joy.bgImg = k_BundleImage(@"key_joy_wasd");
                joy.thumbImg = k_BundleImage(@"key_joy_thumb_normal");
                joy.callback = ^(Direction oldD, Direction newD) {
                    NSLog(@"%lu, %lu", (unsigned long)oldD, (unsigned long)newD);


                    [self configCustomKeyList:oldD new:newD isArrow:NO];
                };
                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
            }
            break;

            case KEY_kb_rock_arrow:{
                JoystickArrowView *joy = [[JoystickArrowView alloc] init];
                joy.bgImg = k_BundleImage(@"key_joy_arrow");
                joy.thumbImg = k_BundleImage(@"key_joy_thumb_normal");

                joy.callback = ^(Direction oldD, Direction newD) {
                    [self configCustomKeyList:oldD new:newD isArrow:YES];
                };

                [self addSubview:joy];

                [joy mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(@(m.top));
                    make.left.equalTo(@(m.left));
                    make.size.mas_equalTo(CGSizeMake(m.width, m.height));
                }];
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

- (void)configCustomKeyList:(Direction)oldD new:(Direction)newD isArrow:(BOOL)isArrow {
    //                    NSDictionary *xDict = @{
    //                            @"inputState": @1,
    //                            @"inputOp": @(1027),
    //                            @"value": @((int)roundf(point.x * 32767))
    //                    };

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
        /// 如果old不是默认值，则要发一个old的抬起
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
