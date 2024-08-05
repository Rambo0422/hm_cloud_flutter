//
//  CloudPreViewController.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import <AVFAudio/AVFAudio.h>
#import "CloudPreViewController.h"


#define kScreenW [UIScreen mainScreen].bounds.size.width
#define kScreenH [UIScreen mainScreen].bounds.size.height

@interface CloudPreViewController ()

@property (weak, nonatomic) IBOutlet UIButton *setBtn;
@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftCos;

@end

@implementation CloudPreViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

    [self configView];
}

- (void)configView {
//    self.soundSwitch.transform = CGAffineTransformMakeScale(0.85, 0.85);

    [self.setBtn setImage:k_BundleImage(@"ic_4g_high") forState:UIControlStateNormal];

    self.rightCos.constant = -kScreenH;
    self.leftCos.constant = -kScreenH;
    self.topCos.constant = -50;


    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideSetView)];
    [self.bgView addGestureRecognizer:tap];
}

- (void)hideSetView {
    __weak typeof(self) weakSelf = self;
    [UIView animateWithDuration:0.25
                     animations:^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        strongSelf.topCos.constant = (strongSelf.topCos.constant == 0) ? -50 : 0;
        strongSelf.leftCos.constant = (strongSelf.leftCos.constant == 0) ? -kScreenH : 0;
        strongSelf.rightCos.constant = (strongSelf.rightCos.constant == 0) ? -kScreenH : 0;

        strongSelf.bgView.alpha = (strongSelf.rightCos.constant == 0) ? 0.3 : 0;

        [self.view layoutIfNeeded];
    }];
}

- (void)viewWillLayoutSubviews {
    self.gameVC.view.frame = self.view.bounds;
    [self.view insertSubview:self.gameVC.view atIndex:0];
}

- (IBAction)didTapSet:(id)sender {
    [self hideSetView];
}

- (UIRectEdge)preferredScreenEdgesDeferringSystemGestures {
    return UIRectEdgeAll;
}

- (IBAction)didTapDismiss:(id)sender {
    if (self.didDismiss) {
        [self.view.subviews.firstObject removeFromSuperview];
        self.didDismiss();
    }

    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)didTapPushPage:(id)sender {
    if (self.pushFlutter) {
        self.pushFlutter();
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void)dealloc {
    NSLog(@"CloudPreViewController dealloc");
}

- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    // 如果该界面需要支持横竖屏切换
    return UIInterfaceOrientationMaskLandscapeRight;
}

@end
