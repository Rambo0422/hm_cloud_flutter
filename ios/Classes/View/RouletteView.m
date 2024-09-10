//
//  RouletteView.m
//  hm_cloud-SanA_Game
//
//  Created by a水 on 2024/8/20.
//

#import "RouletteView.h"
#import "SanA_Macro.h"

@interface RouletteView ()

@property (nonatomic, strong) UIColor *highlightColor;
@property (nonatomic, assign) NSInteger numberOfSegments;
@property (nonatomic, strong) UIButton *centerButton;

@end

@implementation RouletteView{
    NSInteger _highlightedSegment;

    BOOL _isVisible; // 用于跟踪圆形视图是否可见
}

- (instancetype)initWithEidt:(BOOL)isEdit model:(KeyModel *)model {
    self = [super initWithEidt:isEdit model:model];

    if (self) {
        _numberOfSegments = model.rouArr.count;
        _highlightColor = [kColor(0xC6EC4B) colorWithAlphaComponent:0.45];
        _highlightedSegment = -1; // 没有高亮的部分
        _isVisible = NO; // 默认圆形视图不可见
        self.backgroundColor = [UIColor clearColor];
        [self setupCenterButton];

        if (self.isEdit) {
            UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
            pan.delaysTouchesBegan = YES;
            pan.delaysTouchesEnded = YES;
            [self addGestureRecognizer:pan];
        }
    }

    return self;
}

- (void)setupCenterButton {
    CGFloat buttonSize = 45; // 中心按钮的大小，可以根据需要调整

    self.centerButton = [UIButton buttonWithType:UIButtonTypeCustom];
    self.centerButton.frame = CGRectMake((self.bounds.size.width - buttonSize) / 2, (self.bounds.size.height - buttonSize) / 2, buttonSize, buttonSize);

    self.centerButton.layer.cornerRadius = buttonSize / 2;

    [self.contentView addSubview:self.centerButton];

    @weakify(self);
    [RACObserve(self.model, text) subscribeNext:^(id _Nullable x) {
        @strongify(self);

        [self.centerButton setTitle:self.model.text
                           forState:UIControlStateNormal];
    }];

    [self.centerButton setBackgroundImage:k_BundleImage(@"key_rout_n") forState:UIControlStateNormal];
    [self.centerButton setBackgroundImage:k_BundleImage(@"key_rout_h") forState:UIControlStateHighlighted];


    [self.centerButton setTitleColor:[kColor(0xFFFFFF) colorWithAlphaComponent:0.6] forState:UIControlStateNormal];
    self.centerButton.titleLabel.font = [UIFont systemFontOfSize:9];
}

- (void)handlePan:(UIPanGestureRecognizer *)gestureRecognizer {
    if (self.isEdit) {
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


        self.model.top = top / (kScreenH / 375.0);
        self.model.left = left / (kScreenW / 667.0);


        // 重置拖拽手势的累积位移
        [gestureRecognizer setTranslation:CGPointZero inView:self.superview];

        if (self.tapCallback) {
            self.tapCallback(self.model);
        }
    }
}

- (void)updateHighlightedSegmentForPoint:(CGPoint)point {
    CGPoint center = CGPointMake(self.bounds.size.width / 2, self.bounds.size.height / 2);
    CGFloat dx = point.x - center.x;
    CGFloat dy = point.y - center.y;
    CGFloat angle = atan2(dy, dx);

    if (angle < 0) {
        angle += 2 * M_PI;
    }

    NSInteger segment = angle / (2 * M_PI / self.numberOfSegments);

    if (_highlightedSegment != segment) {
        _highlightedSegment = segment;
        [self setNeedsDisplay]; // 重新绘制以高亮正确的部分
    }
}

- (void)drawRect:(CGRect)rect {
    if (!_isVisible) {
        return; // 如果圆形视图不可见，则直接返回，不进行绘制
    }

    CGFloat radius = MIN(self.bounds.size.width, self.bounds.size.height) / 2;
    CGFloat innerRadius = 25; // 定义内圆半径
    CGPoint center = CGPointMake(self.bounds.size.width / 2, self.bounds.size.height / 2);
    CGFloat angleStep = 2 * M_PI / self.numberOfSegments;

    CGFloat gapAngle = 1.0 / radius; // 将间隔大小定义为1像素宽的弧度

    for (NSInteger i = 0; i < self.numberOfSegments; i++) {
        CGFloat startAngle = angleStep * i + gapAngle / 2;
        CGFloat endAngle = startAngle + angleStep - gapAngle;

        UIBezierPath *segmentPath = [UIBezierPath bezierPath];

        // 从内圆的起始角度处开始
        [segmentPath moveToPoint:CGPointMake(center.x + innerRadius * cos(startAngle),
                                             center.y + innerRadius * sin(startAngle))];

        // 添加内圆到外圆的路径
        [segmentPath addArcWithCenter:center radius:radius startAngle:startAngle endAngle:endAngle clockwise:YES];

        // 添加外圆到内圆的路径
        [segmentPath addArcWithCenter:center radius:innerRadius startAngle:endAngle endAngle:startAngle clockwise:NO];

        [segmentPath closePath];

        if (i == _highlightedSegment) {
            [self.highlightColor setFill];
        } else {
            [[[UIColor blackColor] colorWithAlphaComponent:0.3] setFill];
        }

        [segmentPath fill];

        // 在每个分段区域的中心绘制标题
        NSString *title = self.model.rouArr[i].text;
        CGPoint titlePosition = [self positionForTitleAtIndex:i withRadius:(radius + innerRadius) / 2 center:center angleStep:angleStep];
        UIFont *font = [UIFont systemFontOfSize:9];
        NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
        style.alignment = NSTextAlignmentCenter;
        NSDictionary *attributes = @{
                NSFontAttributeName: font,
                NSForegroundColorAttributeName: [UIColor whiteColor],
                NSParagraphStyleAttributeName: style
        };
        CGSize titleSize = [title sizeWithAttributes:attributes];
        CGRect titleRect = CGRectMake(titlePosition.x - titleSize.width / 2, titlePosition.y - titleSize.height / 2, titleSize.width, titleSize.height);
        [title drawInRect:titleRect withAttributes:attributes];
    }
}

- (CGPoint)positionForTitleAtIndex:(NSInteger)index withRadius:(CGFloat)radius center:(CGPoint)center angleStep:(CGFloat)angleStep {
    CGFloat middleAngle = angleStep * index + angleStep / 2;
    CGFloat titleRadius = radius;  // 标题离圆心的距离，可以根据需要调整
    CGFloat x = center.x + cos(middleAngle) * titleRadius;
    CGFloat y = center.y + sin(middleAngle) * titleRadius;

    return CGPointMake(x, y);
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    if (CGRectContainsPoint(_centerButton.frame, point)) {
        return self;
    }

    if (!_isVisible) {
        return nil;
    }

    return [super hitTest:point withEvent:event];
}

#pragma mark - Touch handling

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    UITouch *touch = [touches anyObject];
    CGPoint point = [touch locationInView:self];

    if (CGRectContainsPoint(self.centerButton.frame, point)) {
        _isVisible = YES;
        self.centerButton.highlighted = YES;
        [self setNeedsDisplay];
    }
}

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (!_isVisible) {
        return;              // 如果圆形视图不可见，直接返回
    }

    UITouch *touch = [touches anyObject];
    CGPoint point = [touch locationInView:self];

    if (CGRectContainsPoint(self.centerButton.frame, point)) {
        _highlightedSegment = -1;
        [self setNeedsDisplay];
    } else {
        [self updateHighlightedSegmentForPoint:point];
    }
}

- (void)touchesCancelled:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    _isVisible = NO;

    _highlightedSegment = -1;
    self.centerButton.highlighted = NO;
    [self setNeedsDisplay];
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    _isVisible = NO;

    if (_highlightedSegment != -1) {
        [self touchDown:self.model.rouArr[_highlightedSegment]];

        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self touchUp:self.model.rouArr[self->_highlightedSegment]];
            self->_highlightedSegment = -1;
        });
    }

    self.centerButton.highlighted = NO;
    [self setNeedsDisplay];
}

- (void)touchDown:(KeyModel *)m {
    if ([HmCloudTool share].isVibration) {
        UIImpactFeedbackGenerator *mediumImpact = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleMedium];
        [mediumImpact impactOccurred];
    }

    NSDictionary *dict;

    if ([m.type containsString:@"xbox-"]) {
        dict = [self xboxKeyDown:m];
    } else if ([self.model.type containsString:@"kb-mouse"]) {
        dict = [self mouseKeyDown:m];
    } else {
        dict = [self keyboardKeyDown:m];
    }

    if (self.downCallback) {
        self.downCallback(@[dict]);
    }
}

- (void)touchUp:(KeyModel *)m {
    NSDictionary *dict;

    if ([self.model.type containsString:@"xbox-"]) {
        dict = [self xboxKeyUp:m];
    } else if ([self.model.type containsString:@"kb-mouse"]) {
        // 鼠标 按键

        dict = [self mouseKeyUp:m];
    } else {
        dict = [self keyboardKeyUp:m];
    }

    if (self.upCallback) {
        self.upCallback(@[dict]);
    }
}

@end
