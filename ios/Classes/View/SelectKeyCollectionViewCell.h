//
//  SelectKeyCollectionViewCell.h
//  hm_cloud
//
//  Created by a水 on 2024/9/14.
//

#import <UIKit/UIKit.h>
#import "KeyDetailModel.h"

typedef void (^ActionCallback)(KeyDetailModel *_Nullable model);

NS_ASSUME_NONNULL_BEGIN

@interface SelectKeyCollectionViewCell : UICollectionViewCell

@property (nonatomic, strong, nullable) KeyDetailModel *model;

@property (nonatomic, strong) ActionCallback delCallback;
@property (nonatomic, strong) ActionCallback addCallback;
@property (nonatomic, strong) ActionCallback editCallback;
@property (nonatomic, strong) ActionCallback useCallback;

@end

NS_ASSUME_NONNULL_END
