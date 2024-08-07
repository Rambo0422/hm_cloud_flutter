//
//  CloudPreViewController.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import "CloudPreViewController.h"
#import "GameKeyView.h"
#import "GameKeyView.h"
#import "HmCloudTool.h"
#import "RequestTool.h"


@interface CustomSlider : UISlider

@end


@implementation CustomSlider

// 重写 trackRect(forBounds:) 方法
- (CGRect)trackRectForBounds:(CGRect)bounds {
    // 调用父类的实现来获取默认的轨道 rect
    CGRect defaultRect = [super trackRectForBounds:bounds];

    // 修改轨道的高度
    CGRect customRect = CGRectMake(defaultRect.origin.x, (bounds.size.height - 10) / 2, defaultRect.size.width, 10); // 10 是自定义的高度

    return customRect;
}

@end

@interface CloudPreViewController ()

@property (weak, nonatomic) IBOutlet UIButton *setBtn;
@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftCos;
@property (weak, nonatomic) IBOutlet UILabel *timeLab;
@property (weak, nonatomic) IBOutlet UIView *operationView1;
@property (weak, nonatomic) IBOutlet UIView *operationView2;
@property (weak, nonatomic) IBOutlet UIView *operationView3;
@property (weak, nonatomic) IBOutlet UIView *operationView4;
@property (weak, nonatomic) IBOutlet UISlider *mouseSlider;
@property (weak, nonatomic) IBOutlet UIView *sliderBgView1;
@property (weak, nonatomic) IBOutlet UIView *sliderBgView2;
@property (weak, nonatomic) IBOutlet UILabel *msLab;
@property (weak, nonatomic) IBOutlet UILabel *packetLossLab;
@property (weak, nonatomic) IBOutlet UIImageView *netImgView;

@property (nonatomic, strong) CustomSlider *lightSlider;
@property (nonatomic, strong) CustomSlider *soundSlider;

@property (nonatomic, assign) NSInteger ms;
@property (nonatomic, assign) float packetLoss;
@property (nonatomic, assign) BOOL isWifi;

@property (nonatomic, strong) NSArray<KeyModel *> *keyboardList;
@property (nonatomic, strong) NSArray<KeyModel *> *joystickList;

@property (nonatomic, strong) GameKeyView *keyView;

@end

@implementation CloudPreViewController


- (void)viewDidLoad {
    [super viewDidLoad];

    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        // 一共有四种状态
        switch (status) {
            case AFNetworkReachabilityStatusNotReachable:
                break;

            case AFNetworkReachabilityStatusReachableViaWWAN:{
                self.isWifi = NO;
            }
            break;

            case AFNetworkReachabilityStatusReachableViaWiFi:{
                self.isWifi = YES;
            }
            break;

            case AFNetworkReachabilityStatusUnknown:
            default:
                break;
        }
    }];

    [[AFNetworkReachabilityManager sharedManager] startMonitoring];

    [self configView];
    [self configRac];
    [self request];
}

- (void)viewWillLayoutSubviews {
    self.gameVC.view.frame = self.view.bounds;
    [self.view insertSubview:self.gameVC.view atIndex:0];
}

- (void)configRac {
    @weakify(self);
    [RACObserve(self, ms) subscribeNext:^(id _Nullable x) {
        @strongify(self);

        if (self.ms <= 30) {
            self.msLab.textColor = kColor(0x00D38E);
            self.packetLossLab.textColor = kColor(0x00D38E);
            self.netImgView.image = self.isWifi ? k_BundleImage(@"ic_wifi_high") : k_BundleImage(@"ic_4g_high");
            [self.setBtn setImage:(self.isWifi ? k_BundleImage(@"ic_wifi_high") : k_BundleImage(@"ic_4g_high"))
                         forState:UIControlStateNormal];
        } else if (self.ms > 60) {
            self.msLab.textColor = kColor(0xFF2D2D);
            self.packetLossLab.textColor = kColor(0xFF2D2D);
            self.netImgView.image = self.isWifi ? k_BundleImage(@"ic_wifi_low") : k_BundleImage(@"ic_4g_low");
            [self.setBtn setImage:(self.isWifi ? k_BundleImage(@"ic_wifi_low") : k_BundleImage(@"ic_4g_low"))
                         forState:UIControlStateNormal];
        } else {
            self.msLab.textColor = kColor(0xF7DC00);
            self.packetLossLab.textColor = kColor(0xF7DC00);
            self.netImgView.image = self.isWifi ? k_BundleImage(@"ic_wifi_medium") : k_BundleImage(@"ic_4g_medium");
            [self.setBtn setImage:(self.isWifi ? k_BundleImage(@"ic_wifi_medium") : k_BundleImage(@"ic_4g_medium"))
                         forState:UIControlStateNormal];
        }

        self.msLab.text = [NSString stringWithFormat:@"%ldms", self.ms];
        self.packetLossLab.text = [NSString stringWithFormat:@"%.0f%%", self.packetLoss];
    }];


    [[self.lightSlider rac_signalForControlEvents:UIControlEventValueChanged] subscribeNext:^(__kindof UIControl *_Nullable x) {
        [UIScreen mainScreen].brightness = self.lightSlider.value;
    }];
}

- (void)configView {
//    self.soundSwitch.transform = CGAffineTransformMakeScale(0.85, 0.85);

    self.keyView = [[GameKeyView alloc] init];
    [self.view insertSubview:self.keyView atIndex:0];

    [self.keyView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(@0);
    }];

    // 创建 DateComponentsFormatter
    NSDateComponentsFormatter *formatter = [[NSDateComponentsFormatter alloc] init];

    formatter.unitsStyle = NSDateComponentsFormatterUnitsStylePositional;
    formatter.allowedUnits = NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond;
    formatter.zeroFormattingBehavior = NSDateComponentsFormatterZeroFormattingBehaviorPad;

    // 格式化时间戳
    NSString *formattedTime = [formatter stringFromTimeInterval:[HmCloudTool share].peakTime.intValue];

    self.timeLab.text = formattedTime;

    [self.mouseSlider setThumbImage:k_BundleImage(@"set_mouse_slider_thumb") forState:UIControlStateNormal];

    [self.setBtn setImage:k_BundleImage(@"ic_4g_high") forState:UIControlStateNormal];

    self.rightCos.constant = -kScreenH;
    self.leftCos.constant = -kScreenH;
    self.topCos.constant = -50;
    self.bgView.alpha = 0;

    self.operationView1.layer.cornerRadius = 5;
    self.operationView2.layer.cornerRadius = 5;
    self.operationView3.layer.cornerRadius = 5;
    self.operationView4.layer.cornerRadius = 5;

    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideSetView)];
    [self.bgView addGestureRecognizer:tap];

    // 配置亮度slider
    self.lightSlider = [[CustomSlider alloc] initWithFrame:CGRectMake(0, 0, 200, 15)];
    self.lightSlider.center = CGPointMake(11, 100);

    [self.lightSlider setThumbImage:k_BundleImage(@"set_slider_thumb") forState:UIControlStateNormal];
    self.lightSlider.transform = CGAffineTransformMakeRotation(-M_PI_2);

    [self.lightSlider setMaximumTrackTintColor:[UIColor colorWithRed:41.0 / 255 green:45.0 / 255 blue:56.0 / 255 alpha:1]];
    [self.lightSlider setMinimumTrackTintColor:[UIColor colorWithRed:167.0 / 255 green:200.0 / 255 blue:62.0 / 255 alpha:1]];

    self.lightSlider.value = [UIScreen mainScreen].brightness;

    [self.sliderBgView1 addSubview:self.lightSlider];
}

- (void)request {
    NSString *path = [k_SanABundle pathForResource:@"joystick"
                                            ofType:@"json"];
    NSData *data = [NSData dataWithContentsOfFile:path];
    NSError *error;
    NSArray *arr = [NSJSONSerialization JSONObjectWithData:data
                                                   options:kNilOptions
                                                     error:&error];

    if (!error) {
        self.joystickList = [KeyModel mj_objectArrayWithKeyValuesArray:arr];
        self.keyView.keyList = self.joystickList;
    }

    [[RequestTool share] requestUrl:k_api_getKeyboard
                         methodType:Request_GET
                             params:@{ @"type": @"1", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
//        self.joystickList = [KeyModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"keyboard"]];
    }];

    [[RequestTool share] requestUrl:k_api_getKeyboard
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        self.keyboardList = [KeyModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"keyboard"]];

//        self.keyView.keyList = self.keyboardList;
    }];
}

- (void)hideSetView {
    @weakify(self);
    [UIView animateWithDuration:0.25
                     animations:^{
        @strongify(self);
        self.topCos.constant = (self.topCos.constant == 0) ? -50 : 0;
        self.leftCos.constant = (self.leftCos.constant == 0) ? -kScreenH : 0;
        self.rightCos.constant = (self.rightCos.constant == 0) ? -kScreenH : 0;

        self.bgView.alpha = (self.rightCos.constant == 0) ? 0.6 : 0;

        [self.view layoutIfNeeded];
    }];
}

- (IBAction)didTapSet:(id)sender {
    [self hideSetView];
}

- (IBAction)didTapTopup:(id)sender {
    if (self.pushFlutter) {
        self.pushFlutter();
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (IBAction)didTapDismiss:(id)sender {
    if (self.didDismiss) {
        [self.view.subviews.firstObject removeFromSuperview];
        self.didDismiss();
    }

    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)refreshfps:(NSInteger)fps ms:(NSInteger)ms rate:(float)rate packetLoss:(float)packetLoss {
    self.ms = ms;
    self.packetLoss = packetLoss;
}

- (UIRectEdge)preferredScreenEdgesDeferringSystemGestures {
    return UIRectEdgeAll;
}

- (BOOL)shouldAutorotate {
    return YES;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    // 如果该界面需要支持横竖屏切换
    return UIInterfaceOrientationMaskLandscapeRight;
}

- (void)dealloc {
    NSLog(@"CloudPreViewController dealloc");
}

@end
