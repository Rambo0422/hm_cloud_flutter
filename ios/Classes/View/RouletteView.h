//
//  RouletteView.h
//  hm_cloud-SanA_Game
//
//  Created by a水 on 2024/8/20.
//

#import "BaseKeyView.h"

NS_ASSUME_NONNULL_BEGIN

@interface RouletteView : BaseKeyView

@property (nonatomic, strong) void (^ addCallback)(KeyModel *m);


@end

NS_ASSUME_NONNULL_END
