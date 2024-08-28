//
//  NSArray+Category.h
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSArray (Category)


- (NSArray *)mapUsingBlock:(_Nullable id (^)(id obj, NSUInteger idx))block;

- (NSArray *)filter:(BOOL (^)(id obj))block;

@end

NS_ASSUME_NONNULL_END
