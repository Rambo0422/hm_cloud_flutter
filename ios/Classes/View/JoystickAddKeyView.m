//
//  JoystickAddKeyView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/13.
//

#import "JoystickAddKeyView.h"

@implementation JoystickAddKeyView

- (IBAction)didTapKey:(UIButton *)sender {
    KeyModel *m = [[KeyModel alloc] init];



    switch (sender.tag) {
        case 1:{
            // LT
            m.type = @"xbox-square";
            m.inputOp = 1025;
            m.width = 80;
            m.height = 60;
            m.text = @"LT";
        }

        break;

        case 2:{
            // LS
            m.type = @"xbox-round-small";
            m.inputOp = 64;
            m.width = 40;
            m.height = 40;
            m.text = @"LS";
        }

        break;

        case 3:{
            // LB
            m.type = @"xbox-square";
            m.inputOp = 256;
            m.width = 80;
            m.height = 60;
            m.text = @"LB";
        }

        break;

        case 4:{
            // rock_lt
            m.type = @"xbox-rock-lt";

            m.width = 144;
            m.height = 144;
        }

        break;

        case 5:{
            // rock_lt
            m.type = @"xbox-cross";

            m.width = 128;
            m.height = 128;
        }

        break;

        case 6:{
            // xbox-set
            m.type = @"xbox-elliptic";
            m.inputOp = 16;
            m.width = 64;
            m.height = 40;
        }

        break;

        case 7:{
            // xbox-menu
            m.type = @"xbox-elliptic";
            m.inputOp = 32;
            m.width = 64;
            m.height = 40;
        }

        break;

        case 8:{
            // rock_rt
            m.type = @"xbox-rock-rt";

            m.width = 144;
            m.height = 144;
        }

        break;

        case 9:{
            // a
            m.type = @"xbox-round-medium";
            m.inputOp = 4096;
            m.width = 48;
            m.height = 48;
            m.text = @"A";
        }

        break;

        case 10:{
            // b
            m.type = @"xbox-round-medium";
            m.inputOp = 8192;
            m.width = 48;
            m.height = 48;
            m.text = @"B";
        }

        break;

        case 11:{
            // x
            m.type = @"xbox-round-medium";
            m.inputOp = 16384;
            m.width = 48;
            m.height = 48;
            m.text = @"X";
        }

        break;

        case 12:{
            // Y
            m.type = @"xbox-round-medium";
            m.inputOp = 32768;
            m.width = 48;
            m.height = 48;
            m.text = @"Y";
        }

        break;

        case 13:{
            // RB

            m.type = @"xbox-square";
            m.inputOp = 512;
            m.width = 80;
            m.height = 60;
            m.text = @"RB";
        }

        break;

        case 14:{
            // RT

            m.type = @"xbox-square";
            m.inputOp = 1026;
            m.width = 80;
            m.height = 60;
            m.text = @"RT";
        }

        break;

        case 15:{
            // RS
            m.type = @"xbox-round-small";
            m.inputOp = 128;
            m.width = 40;
            m.height = 40;
            m.text = @"RS";
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
