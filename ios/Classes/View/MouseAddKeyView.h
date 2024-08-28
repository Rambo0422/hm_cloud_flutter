//
//  MouseAddKeyView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/14.
//

#import <UIKit/UIKit.h>
#import "KeyModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface MouseAddKeyView : UIView

@property (nonatomic, strong) void (^ addCallback)(KeyModel *m);

@end

NS_ASSUME_NONNULL_END
