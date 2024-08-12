//
//  GameButton.h
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import <UIKit/UIKit.h>
#import "BaseKeyView.h"

NS_ASSUME_NONNULL_BEGIN

typedef void (^TouchUpCallback)(NSArray<NSDictionary *> *keyList);
typedef void (^TouchDownCallback)(NSArray<NSDictionary *> *keyList);

@interface GameButtonView : BaseKeyView

@property (nonatomic, strong) TouchUpCallback upCallback;
@property (nonatomic, strong) TouchDownCallback downCallback;

@end

NS_ASSUME_NONNULL_END
