//
//  CrossView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import "GameKey_CrossView.h"
#import "HmCloudTool.h"
#import "SanA_Macro.h"

@interface GameKey_CrossView ()

@property (strong, nonatomic) UIImageView *imageView;
@property (nonatomic, strong) NSArray<NSString *> *imgList;
@property (nonatomic, strong) NSArray<NSNumber *> *opList;

@end

@implementation GameKey_CrossView

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
    }

    return self;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    // 触发中等震动
    if ([HmCloudTool share].isVibration) {
        UIImpactFeedbackGenerator *mediumImpact = [[UIImpactFeedbackGenerator alloc] initWithStyle:UIImpactFeedbackStyleMedium];
        [mediumImpact impactOccurred];
    }

    UITouch *touch = [touches anyObject];
    CGPoint location = [touch locationInView:self];



    [self updateImg:location];
}

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    UITouch *touch = [touches anyObject];
    CGPoint location = [touch locationInView:self];

    [self updateImg:location];
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    self.imageView.image = k_BundleImage(@"key_cross_normal");
    self.callback(@0);
}

- (void)touchesCancelled:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    self.imageView.image = k_BundleImage(@"key_cross_normal");
    self.callback(@0);
}

- (void)updateImg:(CGPoint)location {
    // Get the center of the circular view
    CGPoint center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));

    // Calculate the angle of the touch point
    CGFloat dx = location.x - center.x;
    CGFloat dy = location.y - center.y;
    CGFloat angle = atan2(dy, dx) + M_PI; // atan2 returns value between -π and π, so we add π to make it between 0 and 2π

    // Determine which segment the angle falls into-
    CGFloat segmentAngle = 2 * M_PI / 8; // 8 equal segments

    NSInteger segmentIndex = (NSInteger)ceil(angle / segmentAngle);

    if (segmentIndex == 8) {
        segmentIndex = 0;
    }

    self.imageView.image = k_BundleImage(self.imgList[segmentIndex]);
    self.callback(self.opList[segmentIndex]);
}

@end
