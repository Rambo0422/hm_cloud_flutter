//
//  NSArray+Category.m
//  hm_cloud
//
//  Created by a水 on 2024/8/8.
//

#import "NSArray+Category.h"

@implementation NSArray (Category)

- (NSArray *)mapUsingBlock:(_Nullable id (^)(id obj, NSUInteger idx))block {
    NSMutableArray *resultArr = [NSMutableArray arrayWithCapacity:self.count];

    [self enumerateObjectsUsingBlock:^(id _Nonnull obj, NSUInteger idx, BOOL *_Nonnull stop) {
        if (block(obj, idx)) {
            [resultArr addObject:block(obj, idx)];
        }
    }];
    return resultArr;
}

- (NSArray *)filter:(BOOL (^)(id obj))block {
    if (!block || !self) {
        return self;
    }

    NSMutableArray *arr = NSMutableArray.array;

    for (id obj in self) {
        if (block(obj)) {
            [arr addObject:obj];
        }
    }

    return arr.copy;
}

@end
