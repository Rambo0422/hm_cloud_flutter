//
//  CrossView.h
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^CrossCallback)(NSNumber *op);

@interface CrossView : UIView

@property (nonatomic, strong) CrossCallback callback;

@end

NS_ASSUME_NONNULL_END
