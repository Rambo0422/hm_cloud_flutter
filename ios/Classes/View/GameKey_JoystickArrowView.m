//
//  JoystickArrowView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//


#import "GameKey_JoystickArrowView.h"
#import "SanA_Macro.h"
@interface GameKey_JoystickArrowView ()

@property (nonatomic, strong) UIImageView *bgImgView;
@property (nonatomic, strong) UIImageView *thumbImgView;

@property (nonatomic, assign) Direction _d;

@end

@implementation GameKey_JoystickArrowView

- (instancetype)initWithEidt:(BOOL)isEdit model:(nonnull KeyModel *)model {
    self = [super initWithEidt:isEdit model:model];

    if (self) {
        self.bgImgView = [[UIImageView alloc] initWithFrame:CGRectZero];
        [self.contentView addSubview:self.bgImgView];
        [self.bgImgView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(@0);
        }];

        self.thumbImgView = [[UIImageView alloc] initWithFrame:CGRectZero];
        [self.contentView addSubview:self.thumbImgView];
        [self.thumbImgView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(@0);
            make.centerY.equalTo(@0);
        }];


        self._d = DirectionNormal;
    }

    return self;
}

- (void)setBgImg:(UIImage *)bgImg {
    self.bgImgView.image = bgImg;
}

- (void)setThumbImg:(UIImage *)thumbImg {
    self.thumbImgView.image = thumbImg;
}

- (void)reset {
    if (self.callback) {
        self.callback(self._d, DirectionNormal);
        self._d = DirectionNormal;
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    CGPoint location = [[touches anyObject] locationInView:self];

    [self updateCenterViewPositionWithLocation:location];
}

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    CGPoint location = [[touches anyObject] locationInView:self];

    [self updateCenterViewPositionWithLocation:location];
}

- (void)touchesCancelled:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    @weakify(self);
    [UIView animateWithDuration:0.1
                     animations:^{
        @strongify(self);
        self.thumbImgView.center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));

        [self reset];
    }];
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    @weakify(self);
    [UIView animateWithDuration:0.1
                     animations:^{
        @strongify(self);
        self.thumbImgView.center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));

        [self reset];
    }];
}

- (void)updateCenterViewPositionWithLocation:(CGPoint)location {
    // 计算圆心位置
    CGPoint circleCenter = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));

    CGFloat radius = self.frame.size.width / 2;

    // 计算从圆心到手指位置的向量
    CGPoint vector = CGPointMake(location.x - circleCenter.x, location.y - circleCenter.y);
    CGFloat distance = sqrt(vector.x * vector.x + vector.y * vector.y);

    CGPoint movePoint;

    // 限制中心点视图在圆形视图内移动
    if (distance <= radius) {
        movePoint = location;
    } else {
        // 计算限制后的点
        CGFloat clampedX = circleCenter.x + vector.x / distance * radius;
        CGFloat clampedY = circleCenter.y + vector.y / distance * radius;
        movePoint = CGPointMake(clampedX, clampedY);
    }

    self.thumbImgView.center = movePoint;

    // Calculate the angle of the touch point
    CGFloat dx = movePoint.x - circleCenter.x;
    CGFloat dy = movePoint.y - circleCenter.y;
    CGFloat angle = atan2(dy, dx) + M_PI; // atan2 returns value between -π and π, so we add π to make it between 0 and 2π

    // Determine which segment the angle falls into
    CGFloat segmentAngle = 2 * M_PI / 8; // 8 equal segments

    NSInteger segmentIndex = (NSInteger)floor(angle / segmentAngle);

    if (self._d != (Direction)segmentIndex) {
        if (self.callback) {
            self.callback(self._d, (Direction)segmentIndex);
        }

        self._d = (Direction)segmentIndex;
    }
}

@end
