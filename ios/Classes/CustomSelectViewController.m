//
//  CustomSelectViewController.m
//  hm_cloud-SanA_Game
//
//  Created by a水 on 2024/8/9.
//

#import "CustomSelectViewController.h"

@interface CustomSelectViewController ()

@end

@implementation CustomSelectViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (IBAction)didTapKeyboard:(id)sender {
    [self dismissViewControllerAnimated:YES
                             completion:^{
        if (self.selectCallback) {
            self.selectCallback(Custom_keyboard);
        }
    }];
}

- (IBAction)didTapJoystick:(id)sender {
    [self dismissViewControllerAnimated:YES
                             completion:^{
        if (self.selectCallback) {
            self.selectCallback(Custom_joystick);
        }
    }];
}

@end
