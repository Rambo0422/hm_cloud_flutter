//
//  CloudPreViewController.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import "CloudPreViewController.h"
#import <AVFAudio/AVFAudio.h>
#import "WMDragView.h"

@interface CloudPreViewController ()
@property (weak, nonatomic) IBOutlet UIButton *disMissBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightCos;
@property (weak, nonatomic) IBOutlet UISwitch *soundSwitch;

@property (nonatomic, strong) WMDragView    *setView;

@end

@implementation CloudPreViewController

- (WMDragView *)setView {
    if (!_setView) {
        
        self.setView = [[WMDragView alloc] initWithFrame:CGRectMake(100, 100, 40, 40)];
        
        self.setView.imageView.image = k_BundleImage(@"ic_wifi_high");
        self.setView.isKeepBounds = YES;
        self.setView.backgroundColor = [UIColor clearColor];
        __weak typeof(self) weakSelf = self;
        
        self.setView.clickDragViewBlock = ^(WMDragView *dragView) {
            
            __strong typeof(weakSelf) strongSelf = weakSelf;
            
            [UIView animateWithDuration:0.25 animations:^{
               
                strongSelf.rightCos.constant = (strongSelf.rightCos.constant == 0) ? -160 : 0;
                [strongSelf.view layoutIfNeeded];
                
            }];
            
        };
        
    }
    return _setView;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hideSetView) name:@"touchBegan" object:nil];
    
    [self configView];
}

- (void)configView{
    
    self.disMissBtn.layer.cornerRadius = 5.0;
    self.soundSwitch.transform = CGAffineTransformMakeScale(0.85, 0.85);
    
}

- (void)hideSetView {
    [UIView animateWithDuration:0.25 animations:^{
       
        self.rightCos.constant = -160;
        [self.view layoutIfNeeded];
        
    }];
}

- (void)viewWillLayoutSubviews {
    
    self.gameVC.view.frame = self.view.bounds;
    [self.view insertSubview:self.gameVC.view atIndex:0];
}


- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        self.setView.frame = CGRectMake(self.view.frame.size.width, 200, 40, 40);
        [self.view addSubview:self.setView];
    });
}

//- (BOOL)prefersHomeIndicatorAutoHidden {
//    return YES;
//}

- (UIRectEdge)preferredScreenEdgesDeferringSystemGestures{
    return UIRectEdgeAll;
}

- (IBAction)didTapDismiss:(id)sender {
    if (self.didDismiss) {
        [self.view.subviews.firstObject removeFromSuperview];
        self.didDismiss();
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)didChangeSoundSwitch:(UISwitch *)sender {
    // 改变声音
    if (self.channelAction) {
        self.channelAction(k_changeSound, sender.on);
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
