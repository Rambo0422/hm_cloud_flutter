//
//  BaseKeyView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import <UIKit/UIKit.h>
#import "KeyModel.h"
NS_ASSUME_NONNULL_BEGIN

typedef void (^TouchUpCallback)(NSArray<NSDictionary *> *keyList);
typedef void (^TouchDownCallback)(NSArray<NSDictionary *> *keyList);

@interface BaseKeyView : UIView

- (instancetype)initWithEidt:(BOOL)isEdit model:(KeyModel *)model;

@property (nonatomic, strong) KeyModel *model;

@property (nonatomic, strong) UIView *contentView;

@property (nonatomic, assign) BOOL isEdit;

@property (nonatomic, strong) void (^ tapCallback)(KeyModel *m);


@property (nonatomic, strong) TouchUpCallback upCallback;
@property (nonatomic, strong) TouchDownCallback downCallback;

// MARK: xbox 抬起
- (NSDictionary *)xboxKeyUp:(KeyModel *)m;
// MARK: xbox 按下
- (NSDictionary *)xboxKeyDown:(KeyModel *)m;


// MARK: 鼠标 抬起
- (NSDictionary *)mouseKeyUp:(KeyModel *)m;
// MARK: 鼠标 按下
- (NSDictionary *)mouseKeyDown:(KeyModel *)m;


// MARK: 键盘 按下
- (NSDictionary *)keyboardKeyDown:(KeyModel *)m;
// MARK: 键盘 抬起
- (NSDictionary *)keyboardKeyUp:(KeyModel *)m;

@end

NS_ASSUME_NONNULL_END
