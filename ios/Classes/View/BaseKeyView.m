//
//  BaseKeyView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "BaseKeyView.h"
#import "SanA_Macro.h"

@implementation BaseKeyView


- (instancetype)initWithEidt:(BOOL)isEdit model:(KeyModel *)model
{
    self = [super init];

    if (self) {
        self.isEdit = isEdit;
        self.model = model;
        self.contentView = [[UIView alloc] init];
        self.contentView.frame = self.bounds;
        self.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self addSubview:self.contentView];


        self.frame = CGRectMake(self.model.left, self.model.top, self.model.width, self.model.height);

        if (self.isEdit) {
            self.contentView.userInteractionEnabled = NO;
            UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tap:)];
            [self addGestureRecognizer:tap];
        }

        @weakify(self);
        [RACObserve(self.model, zoom) subscribeNext:^(id _Nullable x) {
            @strongify(self);
            self.bounds = CGRectMake(0, 0, self.model.width, self.model.height);
        }];

        [RACObserve(self.model, opacity) subscribeNext:^(id _Nullable x) {
            @strongify(self);
            self.alpha = self.model.opacity / 100.0;
        }];
    }

    return self;
}

- (void)tap:(UITapGestureRecognizer *)tap {
    if (self.tapCallback) {
        self.tapCallback(self.model);
    }
}

@end
