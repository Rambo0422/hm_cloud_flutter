//
//  GameKey_ContainerView.m
//  hm_cloud
//
//  Created by a水 on 2024/12/16.
//

#import "GameKey_ButtonView.h"
#import "GameKey_ContainerView.h"
#import "SanA_Macro.h"

@interface GameKey_ContainerView ()

@property (nonatomic, strong) UIButton *arrowBtn;

@property (nonatomic, assign) BOOL isShow;

@end

@implementation GameKey_ContainerView


- (instancetype)initWithEidt:(BOOL)isEdit model:(KeyModel *)model {
    self = [super initWithEidt:isEdit model:model];

    if (self) {
        @weakify(self);
        [RACObserve(self.model, zoom) subscribeNext:^(id _Nullable x) {
            @strongify(self);
            CGFloat w = self.model.containerArr.count * (self.model.height + 10) + self.model.width;

            self.frame = CGRectMake(self.model.left, self.model.top, w, self.model.height);
            [self refreshView];
        }];
    }

    return self;
}

- (void)refreshView {
    CGFloat w = self.model.containerArr.count * (self.model.height + 10) + self.model.width;

    // 获取屏幕的中心点
    CGPoint center = CGPointMake([UIScreen mainScreen].bounds.size.width / 2, [UIScreen mainScreen].bounds.size.height / 2);

    // 判断箭头是在左边还是右边
    BOOL isLeft = center.x > self.center.x;

    // 从父视图中移除 所有子视图
    [self.contentView.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];

    for (int a = 0; a < self.model.containerArr.count; a++) {
        KeyModel *m = self.model.containerArr[a];
        GameKey_ButtonView *btn = [[GameKey_ButtonView alloc] initWithEidt:NO
                                                                     model:m];

        if (self.isEdit) {
            btn.hidden = NO;
        } else {
            btn.hidden = !self.isShow;
        }

        btn.frame = CGRectMake(a * (self.model.height + 10) + (isLeft ? (self.model.width + 10) : 0), 0, self.model.height, self.model.height);

        btn.upCallback = ^(NSArray<NSDictionary *> *keyList) {
            [[HmCloudTool share] sendCustomKey:keyList];
        };

        btn.downCallback = ^(NSArray<NSDictionary *> *keyList) {
            [[HmCloudTool share] sendCustomKey:keyList];
        };

        [self.contentView addSubview:btn];
    }

    self.arrowBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.arrowBtn.heightAnchor constraintEqualToConstant:self.model.height].active = YES;
    [self.arrowBtn.widthAnchor constraintEqualToConstant:self.model.width].active = YES;
    [self.arrowBtn addTarget:self action:@selector(didTapBtn) forControlEvents:UIControlEventTouchUpInside];

    if (isLeft) {
        [self.arrowBtn setImage:k_BundleImage(self.isShow ? @"key_arrow_left" : @"key_arrow_right")
                       forState:UIControlStateNormal];
    } else {
        [self.arrowBtn setImage:k_BundleImage(self.isShow ? @"key_arrow_right" : @"key_arrow_left")
                       forState:UIControlStateNormal];
    }

    self.arrowBtn.backgroundColor = [kColor(0x000000) colorWithAlphaComponent:0.45];
    self.arrowBtn.layer.borderWidth = 1;
    self.arrowBtn.layer.borderColor = [kColor(0xFFFFFF) colorWithAlphaComponent:0.25].CGColor;

    self.arrowBtn.frame = CGRectMake(isLeft ? 0 : (w - self.model.width), 0, self.model.width, self.model.height);
    [self.contentView addSubview:self.arrowBtn];
}

- (void)didTapBtn {
    self.isShow = !self.isShow;
    [self refreshView];
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *view = [super hitTest:point withEvent:event];

    if ([self pointInside:point withEvent:event]) {
        if (view == self.arrowBtn || self.isShow) {
            return view;
        } else {
            return self.superview;
        }
    } else {
        return view;
    }
}

@end
