//
//  CloudPreViewController.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import "CloudPreViewController.h"
#import <AVFAudio/AVFAudio.h>
@interface CloudPreViewController ()
@property (weak, nonatomic) IBOutlet UIButton *disMissBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightCos;
@property (weak, nonatomic) IBOutlet UISwitch *soundSwitch;

@end

@implementation CloudPreViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.disMissBtn.layer.cornerRadius = 5.0;
    self.soundSwitch.transform = CGAffineTransformMakeScale(0.85, 0.85);
    
}

- (void)viewWillLayoutSubviews {
    
    self.gameVC.view.frame = self.view.bounds;
    [self.view insertSubview:self.gameVC.view atIndex:0];
}

- (IBAction)didTapDismiss:(id)sender {
    if (self.didDismiss) {
        [self.view.subviews.firstObject removeFromSuperview];
        self.didDismiss();
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)didShowAction:(UIButton *)sender {
    
    CGFloat constant;
    if (_rightCos.constant == 0) {
        constant = -160;
    } else {
        constant = 0;
    }
    
    
    [UIView animateWithDuration:0.25 animations:^{
       
        self.rightCos.constant = constant;
        [self.view layoutIfNeeded];
        
    } completion:^(BOOL finished) {
        
        [sender setTitle:(constant == 0 ? @"收起" : @"展开") forState:UIControlStateNormal];
        
    }];
    
}

- (IBAction)didChangeSoundSwitch:(UISwitch *)sender {
    
//    NSError *error2;
//    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord
//                                            mode:AVAudioSessionModeVoiceChat
//                                         options:(AVAudioSessionCategoryOptionMixWithOthers |
//                                                  AVAudioSessionCategoryOptionAllowBluetoothA2DP |
//                                                  AVAudioSessionCategoryOptionAllowAirPlay |
//                                                  AVAudioSessionCategoryOptionDefaultToSpeaker |
//                                                  AVAudioSessionCategoryOptionAllowBluetooth)
//                                           error:&error2];
//    if (error2) {
//        NSLog(@"error2 = %@",error2);
//    }
    
    // 改变声音
    if (self.channelAction) {
        self.channelAction(@"changeSound", sender.on);
    }
}

- (void)dealloc {
    NSLog(@"CloudPreViewController dealloc");
}

- (BOOL)shouldAutorotate{
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    // 如果该界面需要支持横竖屏切换
    return UIInterfaceOrientationMaskLandscapeRight;
}


@end
