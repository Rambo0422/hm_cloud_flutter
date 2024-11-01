//
//  CloudPreViewController.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/6.
//

#import <GameController/GameController.h>
#import "CloudPreViewController.h"
#import "CustomKeyViewController.h"
#import "CustomSelectViewController.h"
#import "CustomSlider.h"
#import "GameDetailsModel.h"
#import "GameKeyContainerView.h"
#import "HmCloudTool.h"
#import "PartyAvatarCollectionViewCell.h"
#import "PartyAvatarTableViewCell.h"
#import "RequestTool.h"

typedef enum : NSUInteger {
    Resolution_BD = 1,
    Resolution_High,
    Resolution_Medium,
    Resolution_Low,
} ResolutionType;

@interface CloudPreViewController ()<UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout, UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UIButton *setBtn;
@property (weak, nonatomic) IBOutlet UIButton *showKeyboardBtn;
@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftCos;
@property (weak, nonatomic) IBOutlet UIButton *hidePartyBtn;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *partyLeftCos;

@property (weak, nonatomic) IBOutlet UILabel *timeLab;
@property (weak, nonatomic) IBOutlet UIView *operationView1;
@property (weak, nonatomic) IBOutlet UIView *operationView2;
@property (weak, nonatomic) IBOutlet UIView *operationView3;
@property (weak, nonatomic) IBOutlet UIView *operationView4;
@property (weak, nonatomic) IBOutlet UIButton *fuzhiBtn;

// 鼠标灵敏度
@property (weak, nonatomic) IBOutlet UISlider *mouseSlider;

@property (weak, nonatomic) IBOutlet UIView *sliderBgView1;
@property (weak, nonatomic) IBOutlet UILabel *msLab;
@property (weak, nonatomic) IBOutlet UILabel *packetLossLab;
@property (weak, nonatomic) IBOutlet UIImageView *netImgView;

@property (weak, nonatomic) IBOutlet UICollectionView *partyAvatarCollectionView;

@property (weak, nonatomic) IBOutlet SPButton *joystickBtn;
@property (weak, nonatomic) IBOutlet SPButton *keyboardBtn;
@property (weak, nonatomic) IBOutlet SPButton *vibrationBtn;
@property (weak, nonatomic) IBOutlet SPButton *customBtn;
@property (weak, nonatomic) IBOutlet SPButton *liveBtn;
@property (weak, nonatomic) IBOutlet SPButton *partyBtn;

// 鼠标设置开关
@property (weak, nonatomic) IBOutlet UISwitch *modeSwitch;
// 鼠标点击
@property (weak, nonatomic) IBOutlet SPButton *modeMouseBtn;
// 触控点击
@property (weak, nonatomic) IBOutlet SPButton *modeScreenBtn;
// 触屏攻击
@property (weak, nonatomic) IBOutlet SPButton *modeSwipeBtn;


@property (nonatomic, strong) CustomSlider *lightSlider;

@property (nonatomic, assign) NSInteger ms;
@property (nonatomic, assign) float packetLoss;
@property (nonatomic, assign) BOOL isWifi;

@property (nonatomic, strong) NSMutableArray<KeyDetailModel *> *keyboardList;
@property (nonatomic, strong) NSMutableArray<KeyDetailModel *> *joystickList;

@property (nonatomic, strong) GameKeyContainerView *keyView;

@property (nonatomic, strong) GameDetailsModel *gameDetails;

/// 当前选中的操作模式（1 = 按键 ； 2 = 手柄）
@property (nonatomic, assign) NSInteger currentOperation;

/// 当前清晰度 1蓝光，2超清，3高清，4标清
/// vip默认是蓝光，非vip默认是标清
@property (nonatomic, assign) ResolutionType resolution;

@property (nonatomic, strong) UIView *overlayView;

@property (weak, nonatomic) IBOutlet UIButton *currentResolutionBtn;
@property (weak, nonatomic) IBOutlet UIButton *lowResolutionBtn;
@property (weak, nonatomic) IBOutlet UIButton *bdResolutionBtn;
@property (weak, nonatomic) IBOutlet UIView *resolutionBgView;

@property (nonatomic, assign) BOOL showKeyboard;


// countDown View
@property (weak, nonatomic) IBOutlet UIView *countDownView;
@property (weak, nonatomic) IBOutlet UILabel *countDownLab;
@property (weak, nonatomic) IBOutlet UIButton *topupBtn;
@property (nonatomic, strong) NSTimer *timer;


@property (nonatomic, assign) BOOL isConnectGameControl;


@property (nonatomic, strong) NSArray<PartyAvatarModel *> *collectionList;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, assign) BOOL isShowPartyView;
@property (nonatomic, assign) BOOL isShowSetView;

@end

@implementation CloudPreViewController{
    NSDateComponentsFormatter *_hourFormatter;
    NSDateComponentsFormatter *_countDownFormatter;
}

- (NSMutableArray<KeyDetailModel *> *)joystickList {
    if (!_joystickList) {
        _joystickList = [NSMutableArray array];
    }

    return _joystickList;
}

- (NSMutableArray<KeyDetailModel *> *)keyboardList {
    if (!_keyboardList) {
        _keyboardList = [NSMutableArray array];
    }

    return _keyboardList;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    @weakify(self);
    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        // 一共有四种状态
        @strongify(self);
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

    [self configGameControllers];
}

- (void)configGameControllers {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(gameControllerDidConnect:) name:GCControllerDidConnectNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(gameControllerDidDisConnect:) name:GCControllerDidDisconnectNotification object:nil];
}

- (void)gameControllerDidConnect:(NSNotification *)noti {
    [SVProgressHUD showInfoWithStatus:@"手柄已连接~"];
    self.isConnectGameControl = YES;
}

- (void)gameControllerDidDisConnect:(NSNotification *)noti {
    [SVProgressHUD showInfoWithStatus:@"手柄已断开~"];
    self.isConnectGameControl = NO;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];

    // 首次安装 创建引导层
    NSUserDefaults *def = [NSUserDefaults standardUserDefaults];

    if (![def boolForKey:k_FirstPlay]) {
        [self showGuideOverlay];
        [def setBool:YES forKey:k_FirstPlay];
    }
}

- (void)showGuideOverlay {
    // 创建覆盖视图
    self.overlayView = [[UIView alloc] initWithFrame:self.view.bounds];
    self.overlayView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];

    // 创建一个路径，除了目标视图之外的区域都是黑色的
    UIBezierPath *path = [UIBezierPath bezierPathWithRect:self.overlayView.bounds];

    // 创建一个圆形的高亮区域
    UIBezierPath *circlePath = [UIBezierPath bezierPathWithRoundedRect:self.setBtn.frame cornerRadius:self.setBtn.bounds.size.height / 2];
    [path appendPath:circlePath];
    path.usesEvenOddFillRule = YES;

    CAShapeLayer *maskLayer = [CAShapeLayer layer];
    maskLayer.path = path.CGPath;
    maskLayer.fillRule = kCAFillRuleEvenOdd;
    self.overlayView.layer.mask = maskLayer;


    // 将引导视图添加到主视图
    [self.view addSubview:self.overlayView];

    // 手指
    UIImageView *guide1Img = [[UIImageView alloc] initWithImage:k_BundleImage(@"ic_guide1")];

    // 文字
    UIImageView *guide2Img = [[UIImageView alloc] initWithImage:k_BundleImage(@"ic_guide2")];

    [self.overlayView addSubview:guide1Img];
    [self.overlayView addSubview:guide2Img];


    [guide1Img mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self.setBtn.mas_bottom).offset(15);
        make.right.equalTo(self.setBtn.mas_left);
    }];

    [guide2Img mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(guide1Img.mas_bottom);
        make.right.equalTo(guide1Img.mas_left).offset(-5);
    }];


    // 添加手势识别器以响应点击
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideGuideOverlay)];
    [self.overlayView addGestureRecognizer:tapGesture];
}

- (void)hideGuideOverlay {
    [self.overlayView removeFromSuperview];
    self.overlayView = nil;
}

- (void)viewWillLayoutSubviews {
    self.gameVC.view.frame = self.view.bounds;
    [self.view insertSubview:self.gameVC.view atIndex:0];
}

- (void)configRac {
    HmCloudTool *tool = [HmCloudTool share];

    @weakify(self);
    @weakify(tool);

    [RACObserve(self, resolution) subscribeNext:^(id _Nullable x) {
        @strongify(self);
        [self.currentResolutionBtn setTitle:(self.resolution == Resolution_BD ? @"蓝光" : @"标清")
                                   forState:UIControlStateNormal];

        [self.lowResolutionBtn setTitleColor:(self.resolution == Resolution_BD ? kColor(0x9EA2AD) : kColor(0xC6EC4B))
                                    forState:UIControlStateNormal];

        [self.bdResolutionBtn setTitleColor:(self.resolution == Resolution_BD ? kColor(0xC6EC4B) : kColor(0x9EA2AD))
                                   forState:UIControlStateNormal];
    }];


    [[self.bdResolutionBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        @strongify(tool);

        if ([tool isVip]) {
            self.resolution = Resolution_BD;
            [tool switchResolution:Resolution_BD];
        } else {
            [NormalAlertView showAlertWithTitle:nil
                                        content:nil
                                   confirmTitle:nil
                                    cancelTitle:nil
                                confirmCallback:^{
                [self pushToFlutterPage:Flutter_rechartVip];
            }
                                 cancelCallback:nil];
        }
    }];

    [[self.lowResolutionBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        @strongify(tool);
        self.resolution = Resolution_Low;
        [tool switchResolution:Resolution_Low];
    }];


    [[self.currentResolutionBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        self.resolutionBgView.hidden = !self.resolutionBgView.hidden;
    }];

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

    [[self.liveBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        @strongify(tool);

        if ([tool isVip]) {
            if (tool.isLiving) {
                [tool stopLiving];
            } else {
                [tool startLiving];
            }
        } else {
            [NormalAlertView showAlertWithTitle:nil
                                        content:nil
                                   confirmTitle:nil
                                    cancelTitle:nil
                                confirmCallback:^{
                [self pushToFlutterPage:Flutter_rechartVip];
            }
                                 cancelCallback:nil];
        }
    }];

    RAC(self.liveBtn, selected) = RACObserve(tool, isLiving);


    [[self.partyBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);
        [self hidePartyView];
    }];

    [[self.vibrationBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        tool.isVibration = !tool.isVibration;
    }];

    RAC(self.vibrationBtn, selected) = RACObserve(tool, isVibration);

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
            [tool updateMouseSensitivity:self.mouseSlider.value * 2];
        }
    }];

    [[self.fuzhiBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(tool);
        NSString *textToCopy = [NSString stringWithFormat:@"cid:%@ , uid:%@", tool.cloudId, tool.userId];
        UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
        pasteboard.string = textToCopy;
        [SVProgressHUD showSuccessWithStatus:@"复制成功~"];
    }];


    [[self.hidePartyBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        @strongify(self);

        self.isShowPartyView = NO;
    }];

    [[RACObserve(self, showKeyboard) merge:RACObserve(self, isConnectGameControl)] subscribeNext:^(id _Nullable x) {
        @strongify(self);

        if ([HmCloudTool share].isAudience) {
            self.keyView.hidden = YES;
        } else {
            self.keyView.hidden = (self.showKeyboard || self.isConnectGameControl);
        }
    }];

    [RACObserve(self, isShowSetView) subscribeNext:^(id _Nullable x) {
        @strongify(self);

        self.resolutionBgView.hidden = YES;

        @weakify(self);
        [UIView animateWithDuration:0.25
                         animations:^{
            @strongify(self);
            self.topCos.constant = !self.isShowSetView ? -50 : 0;
            self.leftCos.constant = !self.isShowSetView ? -kScreenH : 0;
            self.rightCos.constant = !self.isShowSetView ? -kScreenH : 0;

            self.bgView.alpha = !self.isShowSetView ? 0 : 0.6;

            [self.view layoutIfNeeded];
        }];
    }];

    [RACObserve(self, isShowPartyView) subscribeNext:^(id _Nullable x) {
        [UIView animateWithDuration:0.25
                         animations:^{
            @strongify(self);

            self.partyLeftCos.constant = !self.isShowPartyView ? -kScreenH : 0;

            self.bgView.alpha = !self.isShowPartyView ? 0 : 0.6;

            [self.view layoutIfNeeded];
        }];
    }];
}

- (void)configView {
    if ([GCController controllers].count) {
        self.isConnectGameControl = YES;
    }

    [self.partyAvatarCollectionView registerNib:[UINib nibWithNibName:@"PartyAvatarCollectionViewCell" bundle:k_SanABundle] forCellWithReuseIdentifier:@"PartyAvatarCollectionViewCell"];
    self.partyAvatarCollectionView.backgroundColor = [UIColor clearColor];

    [self.tableView registerNib:[UINib nibWithNibName:@"PartyAvatarTableViewCell" bundle:k_SanABundle] forCellReuseIdentifier:@"PartyAvatarTableViewCell"];

    if ([HmCloudTool share].isPartyGame) {
        self.partyAvatarCollectionView.delegate = self;
        self.partyAvatarCollectionView.dataSource = self;

        self.tableView.delegate = self;
        self.tableView.dataSource = self;
    }

    // 初始化清晰度
    self.resolution = [HmCloudTool share].isVip ? Resolution_BD : Resolution_Low;
    [[HmCloudTool share] switchResolution:self.resolution];
    self.resolutionBgView.hidden = YES;

    // 设置switch的大小
    self.modeSwitch.transform = CGAffineTransformMakeScale(0.85, 0.85);

    // 初始化震动按钮
    self.vibrationBtn.selected = [HmCloudTool share].isVibration;

    self.mouseSlider.value = [HmCloudTool share].sensitivity / 2;

    self.fuzhiBtn.layer.cornerRadius = 3.0;

    self.keyView = [[GameKeyContainerView alloc] initWithEdit:NO];
    [self.view insertSubview:self.keyView atIndex:0];

    [self.keyView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(@0);
    }];

    // 创建 DateComponentsFormatter
    _hourFormatter = [[NSDateComponentsFormatter alloc] init];

    _hourFormatter.unitsStyle = NSDateComponentsFormatterUnitsStylePositional;
    _hourFormatter.allowedUnits = NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond;
    _hourFormatter.zeroFormattingBehavior = NSDateComponentsFormatterZeroFormattingBehaviorPad;

    // 格式化时间戳
    NSString *formattedTime = [_hourFormatter stringFromTimeInterval:[HmCloudTool share].peakTime.integerValue];

    self.timeLab.text = formattedTime;

    // 格式化倒计时
    _countDownFormatter = [[NSDateComponentsFormatter alloc] init];

    _countDownFormatter.unitsStyle = NSDateComponentsFormatterUnitsStylePositional;
    _countDownFormatter.allowedUnits =  NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond;
    _countDownFormatter.zeroFormattingBehavior = NSDateComponentsFormatterZeroFormattingBehaviorPad;


    NSString *countDownTime = [_countDownFormatter stringFromTimeInterval:[HmCloudTool share].playTime.integerValue / 1000];

    self.countDownLab.text = countDownTime;

    // 初始化倒计时

    if ([HmCloudTool share].isAudience) {
        self.showKeyboard = YES;
        self.showKeyboardBtn.hidden = YES;
    } else {
        [self configTimer];
    }

    self.topupBtn.layer.cornerRadius = 10;
    self.topupBtn.layer.borderColor = kColor(0x58652B).CGColor;
    self.topupBtn.layer.borderWidth = 1;


    [self.mouseSlider setThumbImage:k_BundleImage(@"set_mouse_slider_thumb") forState:UIControlStateNormal];

    [self.setBtn setImage:k_BundleImage(@"ic_4g_high") forState:UIControlStateNormal];

    self.rightCos.constant = -kScreenH;
    self.leftCos.constant = -kScreenH;
    self.partyLeftCos.constant = -kScreenH;
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


    [self updateRoomInfo:[HmCloudTool share].roomInfo controlInfos:[HmCloudTool share].controlInfos];
}

- (void)configTimer {
    @weakify(self);
    self.timer = [NSTimer scheduledTimerWithTimeInterval:1
                                                 repeats:YES
                                                   block:^(NSTimer *_Nonnull timer) {
        @strongify(self);
        [HmCloudTool share].playTime = @([HmCloudTool share].playTime.integerValue - 1000);

        self.countDownView.hidden = !([HmCloudTool share].playTime.integerValue <= 300000);

        NSString *countDownTime = [self->_countDownFormatter stringFromTimeInterval:[HmCloudTool share].playTime.integerValue / 1000];

        self.countDownLab.text = countDownTime;

        if ([HmCloudTool share].playTime.integerValue <= 0) {
            [self.timer invalidate];
            self.timer = nil;
        }
    }];
}

/// MARK: 网络请求
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

/// MARK: 获取官方手柄 如果没有取本地配置
- (void)getJoystickAndSet:(BOOL)set {
    // 获取手柄配置，如果获取失败 则取本地的默认配置
    [[RequestTool share] requestUrl:k_api_getKeyboard_v2
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
            KeyDetailModel *normalJoystick = [[KeyDetailModel alloc] init];
            normalJoystick.keyboard = [KeyModel mj_objectArrayWithKeyValuesArray:arr];
            normalJoystick.isOfficial = YES;
            normalJoystick.type = TypeJoystick;
            [self.joystickList addObject:normalJoystick];

            [self getCustomJoystick:set];
        }
    }
                    successCallBack:^(id _Nonnull obj) {
        // 有默认手柄 则正常处理，如果没有默认手柄 看上面faildCallback 👆🏻
        KeyDetailModel *normalJoystick = [KeyDetailModel mj_objectWithKeyValues:obj[@"data"]];
        normalJoystick.isOfficial = YES;
        normalJoystick.type = TypeJoystick;
        [self.joystickList addObject:normalJoystick];

        [self getCustomJoystick:set];
    }];
}

/// MARK: 获取自定义手柄
- (void)getCustomJoystick:(BOOL)set {
    [[RequestTool share] requestUrl:k_api_getKeyboard_custom_v2
                         methodType:Request_GET
                             params:@{ @"type": @"1", @"game_id": [HmCloudTool share].gameId, @"page": @"1", @"size": @"3" }
                      faildCallBack:^{
        KeyDetailModel *m = self.joystickList.firstObject;
        m.use = YES;

        if (set) {
            self.keyView.keyList = [m.keyboard mutableCopy];
        }
    }

                    successCallBack:^(id _Nonnull obj) {
        __block BOOL isUseOfficial = YES;

        NSArray *temp = [[KeyDetailModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"datas"]] mapUsingBlock:^id _Nullable (KeyDetailModel *_Nonnull obj, NSUInteger idx) {
            if (obj.use) {
                isUseOfficial = NO;
            }

            return obj;
        }];

        if (isUseOfficial) {
            KeyDetailModel *m = self.joystickList.firstObject;
            m.use = YES;
        }

        [self.joystickList addObjectsFromArray:temp];


        [self.joystickList enumerateObjectsUsingBlock:^(KeyDetailModel *_Nonnull obj, NSUInteger idx, BOOL *_Nonnull stop) {
            if (obj.use) {
                if (set) {
                    self.keyView.keyList = [obj.keyboard mutableCopy];
                }

                *stop = YES;
            }
        }];
    }];
}

- (void)getKeyboardAndSet:(BOOL)set {
    [[RequestTool share] requestUrl:k_api_getKeyboard_v2
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        KeyDetailModel *normalKeyboard = [KeyDetailModel mj_objectWithKeyValues:obj[@"data"]];
        normalKeyboard.isOfficial = YES;
        normalKeyboard.type = TypeKeyboard;
        [self.keyboardList addObject:normalKeyboard];

        [self getCustomKeyboard:set];
    }];
}

/// MARK: 获取自定义键盘
- (void)getCustomKeyboard:(BOOL)set {
    [[RequestTool share] requestUrl:k_api_getKeyboard_custom_v2
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId, @"page": @"1", @"size": @"3" }
                      faildCallBack:^{
        KeyDetailModel *m = self.keyboardList.firstObject;
        m.use = YES;

        if (set) {
            self.keyView.keyList = [m.keyboard mutableCopy];
        }
    }

                    successCallBack:^(id _Nonnull obj) {
        __block BOOL isUseOfficial = YES;

        NSArray *temp = [[KeyDetailModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"datas"]] mapUsingBlock:^id _Nullable (KeyDetailModel *_Nonnull obj, NSUInteger idx) {
            if (obj.use) {
                isUseOfficial = NO;
            }

            return obj;
        }];

        if (isUseOfficial) {
            KeyDetailModel *m = self.keyboardList.firstObject;
            m.use = YES;
        }

        [self.keyboardList addObjectsFromArray:temp];


        [self.keyboardList enumerateObjectsUsingBlock:^(KeyDetailModel *_Nonnull obj, NSUInteger idx, BOOL *_Nonnull stop) {
            if (obj.use) {
                if (set) {
                    self.keyView.keyList = [obj.keyboard mutableCopy];
                }

                *stop = YES;
            }
        }];
    }];
}

/// MARK: 动画消失/显示 设置页
- (void)hideSetView {
    if (self.isShowPartyView) {
        self.isShowPartyView = NO;
        return;
    }

    self.isShowSetView = !self.isShowSetView;
}

/// MARK: 动画消失/显示 派对吧view
- (void)hidePartyView {
    if (self.isShowSetView) {
        self.isShowSetView = NO;
    }

    self.isShowPartyView = !self.isShowPartyView;
}

/// MARK: 推出flutter页面
- (void)pushToFlutterPage:(FlutterPageType)pagetype {
    if (self.pushFlutter) {
        self.pushFlutter(pagetype);
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

/// MARK: 推出自定义键盘
- (void)pushCustomKeyController {
    CustomSelectViewController *vc = [[CustomSelectViewController alloc] initWithNibName:@"CustomSelectViewController"
                                                                                  bundle:k_SanABundle];

    vc.modalPresentationStyle = UIModalPresentationCustom;
    vc.transitioningDelegate = self;
    @weakify(self);

    vc.useCallback = ^(KeyDetailModel *_Nonnull model) {
        @strongify(self);

        if (model.type == TypeJoystick) {
            self.currentOperation = 2;
        }

        if (model.type == TypeKeyboard) {
            self.currentOperation = 1;
        }

        self.keyView.keyList = [model.keyboard mutableCopy];
    };

    vc.pushVipCallback = ^{
        @strongify(self);
        [self pushToFlutterPage:Flutter_rechartVip];
    };

    [self presentViewController:vc
                       animated:YES
                     completion:nil];
}

/// MARK: 点击设置
- (IBAction)didTapSet:(id)sender {
    [self hideSetView];
}

/// MARK: 点击右下角键盘按钮
- (IBAction)didTapShowKeyboard:(id)sender {
    self.showKeyboard = !self.showKeyboard;

    [[HMCloudPlayer sharedCloudPlayer] cloudSwitchKeyboard:self.showKeyboard];
}

/// MARK: 点击充值
- (IBAction)didTapTopup:(id)sender {
    [self pushToFlutterPage:Flutter_rechartTime];
}

/// MARK: 点击结束游戏
- (IBAction)didTapDismiss:(id)sender {
    [NormalAlertView showAlertWithTitle:@"是否结束游戏？"
                                content:@""
                           confirmTitle:@"结束"
                            cancelTitle:@"继续游玩"
                        confirmCallback:^{
        if (self.didDismiss) {
            [self.view.subviews.firstObject removeFromSuperview];
            self.didDismiss();
        }

        [self dismissViewControllerAnimated:YES
                                 completion:nil];
    }
                         cancelCallback:nil];
}

/// MARK: 刷新延迟信息
- (void)refreshfps:(NSInteger)fps ms:(NSInteger)ms rate:(float)rate packetLoss:(float)packetLoss {
    self.ms = ms;
    self.packetLoss = packetLoss;
}

- (void)refreshKeyboardStatus:(CloudPlayerKeyboardStatus)status {
    self.showKeyboard = (status == CloudPlayerKeyboardStatusDidShow);
}

// MARK: 派对吧 更新房间信息
- (void)updateRoomInfo:(NSDictionary *)roomInfo controlInfos:(NSArray *)controlInfos {
    NSArray *tempArr = [[PartyAvatarModel mj_objectArrayWithKeyValuesArray:roomInfo[@"room_status"]] mapUsingBlock:^id _Nullable (PartyAvatarModel *_Nonnull obj, NSUInteger idx) {
        if ([obj.uid isEqualToString:[HmCloudTool share].userId]) {
            obj.isPermission = YES;
        }

        for (NSDictionary *dict in controlInfos) {
            NSString *uid = dict[@"uid"];
            BOOL position = [dict[@"position"] boolValue];

            if ([obj.uid isEqualToString:uid]) {
                obj.isPermission = position;
            }
        }

        return obj;
    }];

    BOOL refresh = NO;

    if (tempArr.count != self.collectionList.count) {
        self.collectionList = tempArr;
        refresh = YES;
    } else {
        for (int a = 0; a < tempArr.count; a++) {
            if (![tempArr[a] isEqual:self.collectionList[a]]) {
                refresh = YES;
                break;
            }
        }
    }

    if (refresh) {
        [self.partyAvatarCollectionView reloadData];
        [self.tableView reloadData];
    }
}

- (void)stopTimer {
    [self.timer invalidate];
    self.timer = nil;
}

- (void)dealloc {
    NSLog(@"CloudPreViewController dealloc");
}

/// MARK: collectionView delegate
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.collectionList.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    PartyAvatarCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"PartyAvatarCollectionViewCell" forIndexPath:indexPath];

    cell.model = self.collectionList[indexPath.row];
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(40, 40);
}

// MARK: tableView delegate
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.collectionList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    PartyAvatarTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"PartyAvatarTableViewCell" forIndexPath:indexPath];

    [cell configViewWithModel:self.collectionList[indexPath.row] index:indexPath.row];

    return cell;
}

@end
