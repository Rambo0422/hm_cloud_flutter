//
//  BaseKeyView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import <UIKit/UIKit.h>
#import "KeyModel.h"
NS_ASSUME_NONNULL_BEGIN

@interface BaseKeyView : UIView

- (instancetype)initWithEidt:(BOOL)isEdit;

@property (nonatomic, strong) KeyModel *model;

@property (nonatomic, strong) UIView *contentView;

@property (nonatomic, assign) BOOL isEdit;

@property (nonatomic, strong) void (^ tapCallback)(KeyModel *m);

@end

NS_ASSUME_NONNULL_END
