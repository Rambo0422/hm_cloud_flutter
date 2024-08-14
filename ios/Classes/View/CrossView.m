//
//  CrossView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import "CrossView.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"

@interface CrossView ()

@property (strong, nonatomic) UIImageView *imageView;
@property (nonatomic, strong) NSArray<NSString *> *imgList;
@property (nonatomic, strong) NSArray<NSNumber *> *opList;

@end

@implementation CrossView

- (instancetype)initWithEidt:(BOOL)isEdit model:(nonnull KeyModel *)model {
    self = [super initWithEidt:isEdit model:model];

    if (self) {
        self.imageView = [[UIImageView alloc] initWithFrame:CGRectZero];
        self.imageView.image = k_BundleImage(@"key_cross_normal");
        [self.contentView addSubview:self.imageView];

        [self.imageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(@0);
        }];


        self.imgList = @[
            @"key_cross_left",
            @"key_cross_top_left",
            @"key_cross_top",
            @"key_cross_top_right",
            @"key_cross_right",
            @"key_cross_bottom_right",
            @"key_cross_bottom",
            @"key_cross_bottom_left",
        ];

        self.opList = @[@4, @5, @1, @9, @8, @10, @2, @6];

        UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
        [self addGestureRecognizer:pan];

        if (!self.isEdit) {
            UILongPressGestureRecognizer *longGe = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLong:)];
            longGe.minimumPressDuration = 0; // 设置为 0，立即触发
            [self addGestureRecognizer:longGe];
        }
    }

    return self;
}

- (void)handleLong:(UILongPressGestureRecognizer *)longGe {
    if (longGe.state == UIGestureRecognizerStateBegan) {
        // 触发中等震动
        if ([HmCloudTool share].isVibration) {
            UIImpactFeedbackGenerator *mediumImpact = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleMedium];
            [mediumImpact impactOccurred];
        }

        CGPoint location = [longGe locationInView:self];

        [self updateImg:location];
    } else if (longGe.state == UIGestureRecognizerStateEnded || longGe.state == UIGestureRecognizerStateCancelled) {
        // 在手势结束时将中心视图移回圆心

        self.imageView.image = k_BundleImage(@"key_cross_normal");
        self.callback(@0);
    }
}

- (void)handlePan:(UIPanGestureRecognizer *)gestureRecognizer {
    if (self.isEdit) {
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
    } else {
        CGPoint location = [gestureRecognizer locationInView:self];


        [self updateImg:location];

        // 在手势结束时将中心视图移回圆心
        if (gestureRecognizer.state == UIGestureRecognizerStateEnded) {
            self.imageView.image = k_BundleImage(@"key_cross_normal");
            self.callback(@0);
        }
    }
}

- (void)updateImg:(CGPoint)location {
    // Get the center of the circular view
    CGPoint center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));

    // Calculate the angle of the touch point
    CGFloat dx = location.x - center.x;
    CGFloat dy = location.y - center.y;
    CGFloat angle = atan2(dy, dx) + M_PI; // atan2 returns value between -π and π, so we add π to make it between 0 and 2π

    // Determine which segment the angle falls into
    CGFloat segmentAngle = 2 * M_PI / 8; // 8 equal segments

    NSInteger segmentIndex = (NSInteger)floor(angle / segmentAngle);

    if (segmentIndex < self.imgList.count) {
        self.imageView.image = k_BundleImage(self.imgList[segmentIndex]);
        self.callback(self.opList[segmentIndex]);
    }
}

@end
