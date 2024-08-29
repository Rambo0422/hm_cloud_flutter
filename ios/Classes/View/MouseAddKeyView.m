//
//  MouseAddKeyView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/14.
//

#import "MouseAddKeyView.h"

@implementation MouseAddKeyView


- (IBAction)didTapKey:(UIButton *)sender {
    KeyModel *m = [[KeyModel alloc] init];

    m.width = 65;
    m.height = 65;

    switch (sender.tag) {
        case 1:{
            m.type = @"kb-mouse-lt";
            m.inputOp = 512;
        }

        break;

        case 2:{
            m.type = @"kb-mouse-rt";
            m.inputOp = 514;
        }

        break;

        case 3:{
            m.type = @"kb-mouse-md";
            m.inputOp = 513;
        }

        break;

        case 4:{
            m.type = @"kb-mouse-up";
            m.inputOp = 515;
        }

        break;

        case 5:{
            m.type = @"kb-mouse-down";
            m.inputOp = 515;
        }

        break;

        case 6:{
            m.type = @"kb-rock-arrow";
            m.width = 100;
            m.height = 100;
        }

        break;

        case 7:{
            m.type = @"kb-rock-letter";
            m.width = 100;
            m.height = 100;
        }

        break;

        default:
            break;
    }

    m.opacity = 70;
    m.zoom = 50;


    m.click = 0;
    m.left = (668 / 2) - 30;
    m.top = (376 / 2) - 50;

    if (self.addCallback) {
        self.addCallback(m);
    }
}

@end
