//
//  GameKeyView.m
//  AFNetworking
//
//  Created by a水 on 2024/8/7.
//


#import "GameKey_ButtonView.h"
#import "GameKey_CrossView.h"
#import "GameKey_JoystickArrowView.h"
#import "GameKey_JoystickView.h"
#import "GameKey_RouletteView.h"
#import "GameKeyContainerView.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"

@interface AlignmentLineView : UIView
@end

@implementation AlignmentLineView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];

    if (self) {
        self.backgroundColor = [UIColor clearColor];  // 辅助线的颜色
    }

    return self;
}

// 重写 drawRect: 方法以绘制虚线
- (void)drawRect:(CGRect)rect {
    CGContextRef context = UIGraphicsGetCurrentContext();

    CGContextSetLineWidth(context, 1.0);
    CGContextSetStrokeColorWithColor(context, kColor(0xC6EC4B).CGColor); // 虚线的颜色

    CGFloat dashPattern[] = {
        5.0, 3.0
    };                                  // 虚线的样式：5像素线段，3像素间隔
    CGContextSetLineDash(context, 0.0, dashPattern, 2); // 设置虚线

    CGContextMoveToPoint(context, 0, 0);

    // 根据视图的宽高绘制横向或纵向的线条
    if (rect.size.width > rect.size.height) {
        // 横线
        CGContextAddLineToPoint(context, rect.size.width, 0);
    } else {
        // 竖线
        CGContextAddLineToPoint(context, 0, rect.size.height);
    }

    CGContextStrokePath(context); // 绘制路径
}

@end

@interface GameKeyContainerView () {
    BOOL _isEdit;
}

@property (nonatomic, strong) AlignmentLineView *horizontalLine;
@property (nonatomic, strong) AlignmentLineView *verticalLine;
@property (nonatomic, strong) AlignmentLineView *topAlignmentLine;
@property (nonatomic, strong) AlignmentLineView *bottomAlignmentLine;
@property (nonatomic, strong) AlignmentLineView *leftAlignmentLine;
@property (nonatomic, strong) AlignmentLineView *rightAlignmentLine;

@property (nonatomic, assign) CGPoint lastAlignedPoint; // 保存对齐时的拖拽位置
@property (nonatomic, assign) BOOL isAligned; // 标记是否已经对齐

@property (nonatomic, assign) CGPoint accumulatedTranslation; // 保存累计的手势偏移
@property (nonatomic, assign) CGFloat offset;

@end

@implementation GameKeyContainerView

- (instancetype)initWithEdit:(BOOL)isEdit
{
    self = [super init];

    if (self) {
        _isEdit = isEdit;
        [self configView];
    }

    return self;
}

- (void)configView {
    self.offset = 20;

    CGSize size = [UIScreen mainScreen].bounds.size;

    // 创建水平和垂直对齐线，以及左右、上下额外的线条
    self.horizontalLine = [[AlignmentLineView alloc] initWithFrame:CGRectMake(0, 0, size.width, 1)];
    self.verticalLine = [[AlignmentLineView alloc] initWithFrame:CGRectMake(0, 0, 1, size.height)];

    self.topAlignmentLine = [[AlignmentLineView alloc] initWithFrame:CGRectMake(0, 0, size.width, 1)];
    self.bottomAlignmentLine = [[AlignmentLineView alloc] initWithFrame:CGRectMake(0, 0, size.width, 1)];
    self.leftAlignmentLine = [[AlignmentLineView alloc] initWithFrame:CGRectMake(0, 0, 1, size.height)];
    self.rightAlignmentLine = [[AlignmentLineView alloc] initWithFrame:CGRectMake(0, 0, 1, size.height)];

    self.horizontalLine.hidden = YES;
    self.verticalLine.hidden = YES;

    self.topAlignmentLine.hidden = YES;
    self.bottomAlignmentLine.hidden = YES;
    self.leftAlignmentLine.hidden = YES;
    self.rightAlignmentLine.hidden = YES;

    [self addSubview:self.horizontalLine];
    [self addSubview:self.verticalLine];

    [self addSubview:self.topAlignmentLine];
    [self addSubview:self.bottomAlignmentLine];
    [self addSubview:self.leftAlignmentLine];
    [self addSubview:self.rightAlignmentLine];
}

- (void)clear {
    while (self.subviews.count > 0) [self.subviews.lastObject removeFromSuperview];
}

- (void)removeKey:(KeyModel *)model {
    if ([self.keyList containsObject:model]) {
        [self.keyList removeObject:model];

        [self reloadView];
    }
}

- (void)addKey:(KeyModel *)m {
    [self.keyList addObject:m];
    [self addViewWithModel:m];
}

- (void)setKeyList:(NSMutableArray<KeyModel *> *)keyList {
    _keyList = keyList;

    [self reloadView];
}

- (void)reloadView {
    if (self.keyList.count) {
        while (self.subviews.count > 0) [self.subviews.lastObject removeFromSuperview];
    }

    [self addSubview:self.horizontalLine];
    [self addSubview:self.verticalLine];

    [self addSubview:self.topAlignmentLine];
    [self addSubview:self.bottomAlignmentLine];
    [self addSubview:self.leftAlignmentLine];
    [self addSubview:self.rightAlignmentLine];

    for (KeyModel *m in self.keyList) {
        [self addViewWithModel:m];
    }
}

- (void)addViewWithModel:(KeyModel *)m {
    // 为每个子视图添加拖拽手势
    UIPanGestureRecognizer *panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePanGesture:)];

    panGesture.delaysTouchesBegan = YES;

    switch (m.key_type) {
        case KEY_kb_rock_letter:{
            GameKey_JoystickArrowView *joy = [[GameKey_JoystickArrowView alloc] initWithEidt:_isEdit model:m];
            joy.bgImg = k_BundleImage(@"key_joy_wasd");
            joy.thumbImg = k_BundleImage(@"key_joy_thumb_normal");
            joy.callback = ^(Direction oldD, Direction newD) {
                [self configCustomKeyList:oldD new:newD isArrow:NO];
            };
            joy.tapCallback = self.tapCallback;

            if (_isEdit) {
                [joy addGestureRecognizer:panGesture];
            }

            [self addSubview:joy];
        }
        break;

        case KEY_kb_rock_arrow:{
            GameKey_JoystickArrowView *joy = [[GameKey_JoystickArrowView alloc] initWithEidt:_isEdit model:m];
            joy.bgImg = k_BundleImage(@"key_joy_arrow");
            joy.thumbImg = k_BundleImage(@"key_joy_thumb_normal");
            joy.callback = ^(Direction oldD, Direction newD) {
                [self configCustomKeyList:oldD new:newD isArrow:YES];
            };
            joy.tapCallback = self.tapCallback;

            if (_isEdit) {
                [joy addGestureRecognizer:panGesture];
            }

            [self addSubview:joy];
        }
        break;

        case KEY_kb_xbox_rock_lt:{
            GameKey_JoystickView *joy = [[GameKey_JoystickView alloc] initWithEidt:_isEdit model:m];
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
            joy.tapCallback = self.tapCallback;

            if (_isEdit) {
                [joy addGestureRecognizer:panGesture];
            }

            [self addSubview:joy];
        }
        break;

        case KEY_kb_xbox_rock_rt:{
            GameKey_JoystickView *joy = [[GameKey_JoystickView alloc] initWithEidt:_isEdit model:m];
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
            joy.tapCallback = self.tapCallback;

            if (_isEdit) {
                [joy addGestureRecognizer:panGesture];
            }

            [self addSubview:joy];
        }
        break;

        case KEY_kb_xbox_cross:{
            GameKey_CrossView *cross = [[GameKey_CrossView alloc] initWithEidt:_isEdit model:m];
            cross.callback = ^(NSNumber *_Nonnull op) {
                NSDictionary *crossDict = @{
                        @"inputState": @1,
                        @"inputOp": @(1024),
                        @"value": op,
                };
                [[HmCloudTool share] sendCustomKey:@[crossDict]];
            };
            cross.tapCallback = self.tapCallback;

            if (_isEdit) {
                [cross addGestureRecognizer:panGesture];
            }

            [self addSubview:cross];
        }
        break;

        case KEY_kb_roulette:{
            GameKey_RouletteView *roulette = [[GameKey_RouletteView alloc] initWithEidt:_isEdit model:m];
            roulette.tapCallback = self.tapCallback;

            roulette.upCallback = ^(NSArray<NSDictionary *> *keyList) {
                [[HmCloudTool share] sendCustomKey:keyList];
            };

            roulette.downCallback = ^(NSArray<NSDictionary *> *keyList) {
                [[HmCloudTool share] sendCustomKey:keyList];
            };

            if (_isEdit) {
                [roulette addGestureRecognizer:panGesture];
            }

            [self insertSubview:roulette atIndex:0];
        }
        break;

        case KEY_unknown:{
        }
        break;

        case KEY_xbox_combination:
        case KEY_kb_combination:
        default:{
            [self initKey:m ges:panGesture];
        }
        break;
    }
}

- (void)initKey:(KeyModel *)m ges:(UIPanGestureRecognizer *)panGesture {
    GameKey_ButtonView *btn = [[GameKey_ButtonView alloc] initWithEidt:_isEdit model:m];


    if (_isEdit) {
        [btn addGestureRecognizer:panGesture];
    }

    btn.tapCallback = self.tapCallback;

    btn.upCallback = ^(NSArray<NSDictionary *> *keyList) {
        [[HmCloudTool share] sendCustomKey:keyList];
    };

    btn.downCallback = ^(NSArray<NSDictionary *> *keyList) {
        [[HmCloudTool share] sendCustomKey:keyList];
    };

    [self addSubview:btn];
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

- (void)handlePanGesture:(UIPanGestureRecognizer *)gesture {
    BaseKeyView *draggedView = (BaseKeyView *)gesture.view;
    CGPoint translation = [gesture translationInView:self];

    // 根据拖拽的位移更新视图的位置
    CGPoint newCenter = CGPointZero;

    if (self.isAligned) {
        // 累加偏移量
        self.accumulatedTranslation = CGPointMake(self.accumulatedTranslation.x + translation.x, self.accumulatedTranslation.y + translation.y);

        BOOL isHorizontallyAligned = !self.horizontalLine.hidden || !self.topAlignmentLine.hidden || !self.bottomAlignmentLine.hidden;
        BOOL isVerticallyAligned = !self.verticalLine.hidden || !self.leftAlignmentLine.hidden || !self.rightAlignmentLine.hidden;

        if (isHorizontallyAligned) {
        // 横向对齐时，左右拖动没有限制，但上下拖动要超过15个像素
            if (fabs(self.accumulatedTranslation.y) < self.offset) {
                translation.y = 0;      // 保持水平移动
            }
        }

        if (isVerticallyAligned) {
        // 纵向对齐时，上下拖动没有限制，但左右拖动要超过15个像素
            if (fabs(self.accumulatedTranslation.x) < self.offset) {
                translation.x = 0;      // 保持垂直移动
            }
        }
    } else {
        // 重置偏移
        self.accumulatedTranslation = CGPointZero;
    }

    newCenter = CGPointMake(draggedView.center.x + translation.x, draggedView.center.y + translation.y);

    // 获取屏幕边界
    CGFloat halfWidth = CGRectGetWidth(draggedView.bounds) / 2.0;
    CGFloat halfHeight = CGRectGetHeight(draggedView.bounds) / 2.0;
    CGFloat screenWidth = CGRectGetWidth(draggedView.superview.bounds);
    CGFloat screenHeight = CGRectGetHeight(draggedView.superview.bounds);

    // 确保新中心点不会超出屏幕边界
    newCenter.x = MAX(halfWidth, MIN(screenWidth - halfWidth, newCenter.x));
    newCenter.y = MAX(halfHeight, MIN(screenHeight - halfHeight, newCenter.y));

    // 更新视图位置
    draggedView.center = newCenter;


    NSInteger top = (NSInteger)CGRectGetMinY(draggedView.frame);
    NSInteger left = (NSInteger)CGRectGetMinX(draggedView.frame);


    draggedView.model.top = top / (kScreenH / 375.0);
    draggedView.model.left = left / (kScreenW / 667.0);

    [gesture setTranslation:CGPointZero inView:self];

    if (self.tapCallback) {
        draggedView.backgroundColor = [kColor(0xC6EC4B) colorWithAlphaComponent:0.6];
        self.tapCallback(draggedView.model,draggedView);
    }
    
    // 检测是否对齐
    [self checkForAlignmentWithView:draggedView];

    // 手势结束时隐藏辅助线
    if (gesture.state == UIGestureRecognizerStateEnded) {
        self.horizontalLine.hidden = YES;
        self.verticalLine.hidden = YES;

        self.topAlignmentLine.hidden = YES;
        self.bottomAlignmentLine.hidden = YES;
        self.leftAlignmentLine.hidden = YES;
        self.rightAlignmentLine.hidden = YES;
        self.isAligned = NO;     // 重置对齐状态

        self.accumulatedTranslation = CGPointZero;
    }
}

- (void)checkForAlignmentWithView:(UIView *)draggedView {
    CGFloat draggedViewCenterX = draggedView.center.x;
    CGFloat draggedViewCenterY = draggedView.center.y;
    CGFloat draggedViewTop = CGRectGetMinY(draggedView.frame);
    CGFloat draggedViewBottom = CGRectGetMaxY(draggedView.frame);
    CGFloat draggedViewLeft = CGRectGetMinX(draggedView.frame);
    CGFloat draggedViewRight = CGRectGetMaxX(draggedView.frame);

    CGFloat alignmentThreshold = 1;

    // 重置辅助线状态
    self.horizontalLine.hidden = YES;
    self.verticalLine.hidden = YES;

    self.topAlignmentLine.hidden = YES;
    self.bottomAlignmentLine.hidden = YES;
    self.leftAlignmentLine.hidden = YES;
    self.rightAlignmentLine.hidden = YES;
    self.isAligned = NO;

    // 遍历所有子视图，检查是否有与拖拽视图对齐的情况
    for (UIView *otherView in self.subviews) {
        if (otherView == draggedView || otherView.hidden == YES) {
            continue;     // 跳过自身
        }

        CGFloat otherViewCenterY = otherView.center.y;
        CGFloat otherViewCenterX = otherView.center.x;
        CGFloat otherViewTop = CGRectGetMinY(otherView.frame);
        CGFloat otherViewBottom = CGRectGetMaxY(otherView.frame);
        CGFloat otherViewLeft = CGRectGetMinX(otherView.frame);
        CGFloat otherViewRight = CGRectGetMaxX(otherView.frame);

        // 检查与顶部对齐
        if (fabs(draggedViewTop - otherViewTop) < alignmentThreshold) {
//            CGFloat min = MIN(CGRectGetMinX(otherView.frame), CGRectGetMinX(draggedView.frame));
//            CGFloat max = MAX(CGRectGetMaxX(otherView.frame), CGRectGetMaxX(draggedView.frame));
//            self.topAlignmentLine.frame = CGRectMake(min, otherViewTop, max - min, 1);

            self.topAlignmentLine.center = CGPointMake(self.bounds.size.width / 2, otherViewTop);
            self.topAlignmentLine.hidden = NO;
            self.isAligned = YES;
            self.lastAlignedPoint = draggedView.center;
        }

        // 检查与底部对齐
        if (fabs(draggedViewBottom - otherViewBottom) < alignmentThreshold) {
            self.bottomAlignmentLine.center = CGPointMake(self.bounds.size.width / 2, otherViewBottom);
            self.bottomAlignmentLine.hidden = NO;
            self.isAligned = YES;
            self.lastAlignedPoint = draggedView.center;
        }

        // 检查与左边对齐
        if (fabs(draggedViewLeft - otherViewLeft) < alignmentThreshold) {
            self.leftAlignmentLine.center = CGPointMake(otherViewLeft, self.bounds.size.height / 2);
            self.leftAlignmentLine.hidden = NO;
            self.isAligned = YES;
            self.lastAlignedPoint = draggedView.center;
        }

        // 检查与右边对齐
        if (fabs(draggedViewRight - otherViewRight) < alignmentThreshold) {
            self.rightAlignmentLine.center = CGPointMake(otherViewRight, self.bounds.size.height / 2);
            self.rightAlignmentLine.hidden = NO;
            self.isAligned = YES;
            self.lastAlignedPoint = draggedView.center;
        }

        // 检查水平居中对齐
        if (fabs(draggedViewCenterY - otherViewCenterY) < alignmentThreshold) {
            self.horizontalLine.center = CGPointMake(self.bounds.size.width / 2, otherViewCenterY);
            self.horizontalLine.hidden = NO;
            self.isAligned = YES;
            self.lastAlignedPoint = draggedView.center;
        }

        // 检查垂直居中对齐
        if (fabs(draggedViewCenterX - otherViewCenterX) < alignmentThreshold) {
            self.verticalLine.center = CGPointMake(otherViewCenterX, self.bounds.size.height / 2);
            self.verticalLine.hidden = NO;
            self.isAligned = YES;
            self.lastAlignedPoint = draggedView.center;
        }
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
