//
//  GameKeyView.h
//  AFNetworking
//
//  Created by a水 on 2024/8/7.
//

#import <UIKit/UIKit.h>
#import "KeyModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface GameKeyView : UIView

- (instancetype)initWithEdit:(BOOL)isEdit;

- (void)clear;

@property (nonatomic, strong) NSArray<KeyModel *> *keyList;

@property (nonatomic, strong) void (^ tapCallback)(KeyModel *m);

@end

NS_ASSUME_NONNULL_END
