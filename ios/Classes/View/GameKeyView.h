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

@property (nonatomic, strong) NSArray<KeyModel *> *keyList;

@end

NS_ASSUME_NONNULL_END
