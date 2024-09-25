//
//  KeyboardAddKeyView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/13.
//

#import "KeyboardAddKeyView.h"
#import "SanA_Macro.h"

@interface KeyboardAddKeyView ()

@property (nonatomic, strong) NSDictionary *keyboardDict;

@end

@implementation KeyboardAddKeyView

- (instancetype)init
{
    self = [super init];

    if (self) {
        NSString *path = [k_SanABundle pathForResource:@"keyboard"
                                                ofType:@"json"];
        NSData *data = [NSData dataWithContentsOfFile:path];
        NSError *error;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data
                                                             options:kNilOptions
                                                               error:&error];

        if (!error) {
            self.keyboardDict = dict;
        }
    }

    return self;
}

- (instancetype)initWithCoder:(NSCoder *)coder
{
    self = [super initWithCoder:coder];

    if (self) {
        NSString *path = [k_SanABundle pathForResource:@"keyboard"
                                                ofType:@"json"];
        NSData *data = [NSData dataWithContentsOfFile:path];
        NSError *error;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data
                                                             options:kNilOptions
                                                               error:&error];

        if (!error) {
            self.keyboardDict = dict;
        }
    }

    return self;
}

- (IBAction)didTapKey:(UIButton *)sender {
    NSString *tag = [NSString stringWithFormat:@"%ld", sender.tag];

    if (self.keyboardDict && self.keyboardDict[tag]) {
        NSDictionary *dict = self.keyboardDict[tag];

        KeyModel *m = [[KeyModel alloc] init];
        m.text = dict[@"name"];
        m.opacity = 70;
        m.zoom = 50;
        m.type = @"kb-round";
        m.width = 48;
        m.height = 48;
        m.inputOp = sender.tag;
        m.click = 0;
        m.left = (668 / 2) - 20;
        m.top = (376 / 2) - 40;

        if (self.addCallback) {
            self.addCallback(m);
        }
    }
}

@end
