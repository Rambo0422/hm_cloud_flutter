//
//  KeyModel.m
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import "KeyModel.h"
#import "SanA_Macro.h"

static NSString *const KeyTypeEnumTypeStrings[] = {
    [KEY_unknown] = @"unknown",
    [KEY_mouse_left] = @"kb-mouse-lt",
    [KEY_mouse_right] = @"kb-mouse-rt",
    [KEY_mouse_wheel_center] = @"kb-mouse-md",
    [KEY_mouse_wheel_up] = @"kb-mouse-up",
    [KEY_mouse_wheel_down] = @"kb-mouse-down",

    [KEY_kb_rock_letter] = @"kb-rock-letter",
    [KEY_kb_rock_arrow] = @"kb-rock-arrow",
    [KEY_kb_round] = @"kb-round",


    [KEY_kb_xbox_square] = @"xbox-square",
    [KEY_kb_xbox_round_medium] = @"xbox-round-medium",
    [KEY_kb_xbox_round_small] = @"xbox-round-small",
    [KEY_kb_xbox_elliptic] = @"xbox-elliptic",
    [KEY_kb_xbox_rock_lt] = @"xbox-rock-lt",
    [KEY_kb_xbox_rock_rt] = @"xbox-rock-rt",
    [KEY_kb_xbox_cross] = @"xbox-cross",

    [KEY_kb_combination] = @"kb-combination",
    [KEY_xbox_combination] = @"xbox-combination",
    [KEY_kb_roulette] = @"kb-roulette",
};

KeyType KeyTypeFromString(NSString *string) {
    for (NSInteger i = 0; i < sizeof(KeyTypeEnumTypeStrings) / sizeof(KeyTypeEnumTypeStrings[0]); i++) {
        if ([string isEqualToString:KeyTypeEnumTypeStrings[i]]) {
            return (KeyType)i;
        }
    }

    return KEY_unknown;
}

@implementation KeyModel

+ (NSDictionary *)mj_objectClassInArray {
    return @{
        @"composeArr": [KeyModel class],
        @"rouArr": [KeyModel class],
    };
}

- (void)setType:(NSString *)type {
    _type = type;

    self.key_type = KeyTypeFromString(type);
}

- (NSDictionary *)toJson {
    NSMutableDictionary *dict = @{
            @"top": @(_top),
            @"left": @(_left),
            @"type": _type,
            @"zoom": @(_zoom),
            @"opacity": @(_opacity),
            @"click": @(_click),
            @"inputOp": @(_inputOp),
            @"width": @(_width),
            @"height": @(_height),
            @"text": _text ? : @"",
            @"editIndex": @(_editIndex),
            @"isRou": @(_isRou),
        }.mutableCopy;

    if (self.rouArr) {
        [dict setObject:[self rouArrToJson] forKey:@"rouArr"];
    }

    if (self.composeArr) {
        [dict setObject:[self composeToJson] forKey:@"composeArr"];
    }

    return dict;
}

- (NSArray<NSDictionary *> *)rouArrToJson {
    return [self.rouArr mapUsingBlock:^id _Nullable (KeyModel *_Nonnull obj, NSUInteger idx) {
        return [obj toJson];
    }];
}

- (NSArray<NSDictionary *> *)composeToJson {
    return [self.composeArr mapUsingBlock:^id _Nullable (KeyModel *_Nonnull obj, NSUInteger idx) {
        return [obj toJson];
    }];
}

- (NSInteger)top {
    return (NSInteger)(_top * (kScreenH / 375.0));
}

- (NSInteger)left {
    return (NSInteger)(_left * (kScreenW / 667.0));
}

- (NSInteger)width {
    return (NSInteger)(_width * (_zoom / 50.0));
}

- (NSInteger)height {
    return (NSInteger)(_height * (_zoom / 50.0));
}

@end
