//
//  JoystickView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import "JoystickView.h"
#import "SanA_Macro.h"

@interface JoystickView ()

@property (nonatomic, strong) UIImageView *bgImgView;
@property (nonatomic, strong) UIImageView *thumbImgView;


@end

@implementation JoystickView


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

        // 添加拖动手势识别器
        UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
        [self addGestureRecognizer:pan];
    }

    return self;
}

- (void)setBgImg:(UIImage *)bgImg {
    self.bgImgView.image = bgImg;
}

- (void)setThumbImg:(UIImage *)thumbImg {
    self.thumbImgView.image = thumbImg;
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

    //    (_top * (kScreenH / 375.0)) = top;
    //    (_left * (kScreenW / 667.0)) = left;

        self.model.top = top / (kScreenH / 375.0);
        self.model.left = left / (kScreenW / 667.0);


        // 重置拖拽手势的累积位移
        [gestureRecognizer setTranslation:CGPointZero inView:self.superview];

        if (self.tapCallback) {
            self.tapCallback(self.model);
        }
    } else {
        CGPoint location = [gestureRecognizer locationInView:self];

        @weakify(self);
        [self updateCenterViewPositionWithLocation:location];

        // 在手势结束时将中心视图移回圆心
        if (gestureRecognizer.state == UIGestureRecognizerStateEnded) {
            [UIView animateWithDuration:0.1
                             animations:^{
                @strongify(self);
                self.thumbImgView.center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
                self.callback(CGPointMake(0, 0));
            }];
        }
    }
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

    if (self.callback) {
        self.callback(CGPointMake((movePoint.x - radius) / radius, (movePoint.y - radius) / radius));
    }
}

@end
