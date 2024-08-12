//
//  BaseKeyView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "BaseKeyView.h"
#import "SanA_Macro.h"

@implementation BaseKeyView


- (instancetype)initWithEidt:(BOOL)isEdit
{
    self = [super init];

    if (self) {
        self.isEdit = isEdit;
        self.contentView = [[UIView alloc] init];
        self.contentView.frame = self.bounds;
        self.contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self addSubview:self.contentView];

        if (self.isEdit) {
            self.contentView.userInteractionEnabled = NO;
            UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tap:)];
            [self addGestureRecognizer:tap];
        }
    }

    return self;
}

- (void)setModel:(KeyModel *)model {
    _model = model;

    @weakify(self);
    [RACObserve(model, zoom) subscribeNext:^(id _Nullable x) {
        @strongify(self);


        [self mas_remakeConstraints:^(MASConstraintMaker *make) {
            if (self.superview) {
                make.top.equalTo(@(model.top));
                make.left.equalTo(@(model.left));
                make.size.mas_equalTo(CGSizeMake(model.width, model.height));
            }
        }];
    }];

    [RACObserve(model, opacity) subscribeNext:^(id _Nullable x) {
        @strongify(self);
        self.alpha = model.opacity / 100.0;
    }];
}

- (void)tap:(UITapGestureRecognizer *)tap {
    if (self.tapCallback) {
        self.tapCallback(self.model);
    }
}

@end
