//
//  CloudPreViewController.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import "CloudPreViewController.h"
#import "CustomKeyViewController.h"
#import "CustomSelectViewController.h"
#import "CustomSlider.h"
#import "GameDetailsModel.h"
#import "GameKeyView.h"
#import "GameKeyView.h"
#import "HmCloudTool.h"
#import "RequestTool.h"

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
@property (weak, nonatomic) IBOutlet UIButton *fuzhiBtn;

// 鼠标灵敏度
@property (weak, nonatomic) IBOutlet UISlider *mouseSlider;

@property (weak, nonatomic) IBOutlet UIView *sliderBgView1;
@property (weak, nonatomic) IBOutlet UIView *sliderBgView2;
@property (weak, nonatomic) IBOutlet UILabel *msLab;
@property (weak, nonatomic) IBOutlet UILabel *packetLossLab;
@property (weak, nonatomic) IBOutlet UIImageView *netImgView;


@property (weak, nonatomic) IBOutlet SPButton *joystickBtn;
@property (weak, nonatomic) IBOutlet SPButton *keyboardBtn;
@property (weak, nonatomic) IBOutlet SPButton *vibrationBtn;
@property (weak, nonatomic) IBOutlet SPButton *customBtn;

// 鼠标设置开关
@property (weak, nonatomic) IBOutlet UISwitch *modeSwitch;
// 鼠标点击
@property (weak, nonatomic) IBOutlet SPButton *modeMouseBtn;
// 触控点击
@property (weak, nonatomic) IBOutlet SPButton *modeScreenBtn;
// 触屏攻击
@property (weak, nonatomic) IBOutlet SPButton *modeSwipeBtn;


@property (nonatomic, strong) CustomSlider *lightSlider;
@property (nonatomic, strong) CustomSlider *soundSlider;

@property (nonatomic, assign) NSInteger ms;
@property (nonatomic, assign) float packetLoss;
@property (nonatomic, assign) BOOL isWifi;

@property (nonatomic, strong) NSArray<KeyModel *> *keyboardList;
@property (nonatomic, strong) NSArray<KeyModel *> *joystickList;

@property (nonatomic, strong) GameKeyView *keyView;

@property (nonatomic, strong) GameDetailsModel *gameDetails;

/// 当前选中的操作模式（1 = 按键 ； 2 = 手柄）
@property (nonatomic, assign) NSInteger currentOperation;


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
        @strongify(self);
        [UIScreen mainScreen].brightness = self.lightSlider.value;
    }];

    [RACObserve(self, gameDetails) subscribeNext:^(id _Nullable x) {
        @strongify(self);
        self.joystickBtn.enabled = self.gameDetails.support == 2 || self.gameDetails.support == 3;
        self.keyboardBtn.enabled = self.gameDetails.support == 1 || self.gameDetails.support == 3;
    }];

    [RACObserve(self, currentOperation) subscribeNext:^(id _Nullable x) {
        @strongify(self);

        self.joystickBtn.selected = self.currentOperation == 2;
        self.keyboardBtn.selected = self.currentOperation == 1;
    }];

    [[self.customBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);

        [self hideSetView];

        [self pushCustomKeyController];
    }];

    [[self.joystickBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);

        if (!self.joystickBtn.isSelected) {
            self.currentOperation = 2;
            [self getJoystickAndSet:YES];
        }
    }];

    [[self.keyboardBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);

        if (!self.keyboardBtn.isSelected) {
            self.currentOperation = 1;
            [self getKeyboardAndSet:YES];
        }
    }];

    HmCloudTool *tool = [HmCloudTool share];

    @weakify(tool);
    [[self.vibrationBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        tool.isVibration = !tool.isVibration;
    }];

    RAC(self.vibrationBtn, selected) = RACObserve([HmCloudTool share], isVibration);

    [[self.modeSwitch rac_signalForControlEvents:UIControlEventValueChanged] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        @strongify(tool);

        [tool updateTouchMode:(self.modeSwitch.on ? HMCloudCoreTouchModeMouse : HMCloudCoreTouchModeNone)];
    }];

    [RACObserve(tool, touchMode) subscribeNext:^(id _Nullable x) {
        @strongify(tool);
        @strongify(self);
        switch (tool.touchMode) {
            case HMCloudCoreTouchModeNone:{
                self.modeMouseBtn.enabled = NO;
                self.modeScreenBtn.enabled = NO;
                self.modeSwipeBtn.enabled = NO;
            }
            break;

            case HMCloudCoreTouchModeMouse:
            case HMCloudCoreTouchModeScreen:
            case HMCloudCoreTouchSwipe:{
                self.modeMouseBtn.enabled = YES;
                self.modeScreenBtn.enabled = YES;
                self.modeSwipeBtn.enabled = YES;

                self.modeMouseBtn.selected = (tool.touchMode == HMCloudCoreTouchModeMouse);
                self.modeScreenBtn.selected = (tool.touchMode == HMCloudCoreTouchModeScreen);
                self.modeSwipeBtn.selected = (tool.touchMode == HMCloudCoreTouchSwipe);
            }

            break;

            default:
                break;
        }
    }];

    [[self.modeMouseBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        [tool updateTouchMode:HMCloudCoreTouchModeMouse];
    }];

    [[self.modeScreenBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        [tool updateTouchMode:HMCloudCoreTouchModeScreen];
    }];

    [[self.modeSwipeBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        [tool updateTouchMode:HMCloudCoreTouchSwipe];
    }];

    [[self.mouseSlider rac_signalForControlEvents:UIControlEventValueChanged] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        @strongify(tool);

        if (self.mouseSlider.value < 0.1) {
            [tool updateMouseSensitivity:0.1];
        } else {
            [tool updateMouseSensitivity:self.mouseSlider.value];
        }
    }];

    [[self.fuzhiBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        NSString *textToCopy = [NSString stringWithFormat:@"cid:%@ , uid:%@", tool.cloudId, tool.userId];
        UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
        pasteboard.string = textToCopy;
        [SVProgressHUD showSuccessWithStatus:@"复制成功~"];
    }];
}

- (void)configView {
    self.modeSwitch.transform = CGAffineTransformMakeScale(0.85, 0.85);

    self.vibrationBtn.selected = [HmCloudTool share].isVibration;

    self.mouseSlider.value = [HmCloudTool share].sensitivity;

    self.fuzhiBtn.layer.cornerRadius = 3.0;

    self.keyView = [[GameKeyView alloc] initWithEdit:NO];
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
    [[RequestTool share] requestUrl:k_api_get_game_detail
                         methodType:Request_GET
                             params:@{ @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        self.gameDetails = [GameDetailsModel mj_objectWithKeyValues:obj[@"data"]];

        if (self.gameDetails.support == 1) {
            self.currentOperation = 1;
            [self getKeyboardAndSet:YES];
        } else if (self.gameDetails.support == 2) {
            self.currentOperation = 2;
            [self getJoystickAndSet:YES];
        } else {
            self.currentOperation = 2;
            [self getJoystickAndSet:YES];
            [self getKeyboardAndSet:NO];
        }
    }];
}

- (void)getJoystickAndSet:(BOOL)set {
    // 获取手柄配置，如果获取失败 则取本地的默认配置
    [[RequestTool share] requestUrl:k_api_getKeyboard
                         methodType:Request_GET
                             params:@{ @"type": @"1", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:^{
        NSString *path = [k_SanABundle pathForResource:@"joystick"
                                                ofType:@"json"];
        NSData *data = [NSData dataWithContentsOfFile:path];
        NSError *error;
        NSArray *arr = [NSJSONSerialization JSONObjectWithData:data
                                                       options:kNilOptions
                                                         error:&error];

        if (!error) {
            self.joystickList = [KeyModel mj_objectArrayWithKeyValuesArray:arr];

            if (set) {
                self.keyView.keyList = self.joystickList;
            }
        }
    }
                    successCallBack:^(id _Nonnull obj) {
        self.joystickList = [KeyModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"keyboard"]];

        if (set) {
            self.keyView.keyList = self.joystickList;
        }
    }];
}

- (void)getKeyboardAndSet:(BOOL)set {
    [[RequestTool share] requestUrl:k_api_getKeyboard
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        self.keyboardList = [KeyModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"keyboard"]];

        if (set) {
            self.keyView.keyList = self.keyboardList;
        }
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

- (void)pushCustomKeyController {
    CustomSelectViewController *vc = [[CustomSelectViewController alloc] initWithNibName:@"CustomSelectViewController"
                                                                                  bundle:k_SanABundle];

    vc.modalPresentationStyle = UIModalPresentationCustom;
    vc.transitioningDelegate = self;
    @weakify(self);
    vc.selectCallback = ^(CustomType type) {
        @strongify(self);
        CustomKeyViewController *vc = [[CustomKeyViewController alloc] initWithNibName:@"CustomKeyViewController"
                                                                                bundle:k_SanABundle];
        vc.modalPresentationStyle = UIModalPresentationCustom;
        vc.transitioningDelegate = self;
        vc.type = type;
        vc.dismissCallback = ^(BOOL isSave) {
            self.keyView.hidden = NO;
        };
        [self presentViewController:vc
                           animated:YES
                         completion:nil];

        self.keyView.hidden = YES;
    };

    [self presentViewController:vc
                       animated:YES
                     completion:nil];
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

- (void)dealloc {
    NSLog(@"CloudPreViewController dealloc");
}

@end
