//
//  CustomKeyViewController.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "CustomKeyViewController.h"
#import "GameKeyContainerView.h"
#import "JoystickAddKeyView.h"
#import "KeyboardAddKeyView.h"
#import "MouseAddKeyView.h"

#define k_HideBtnHeight     30
#define k_JoystickHeightCos 125
#define k_KeyboardHeightCos 184

@interface CustomKeyViewController ()

@property (nonatomic, strong) GameKeyContainerView *keyView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *bottomCos;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *addKeyViewHeightCos;

@property (nonatomic, assign) NSInteger totalBottomHeight;

@property (nonatomic, strong) JoystickAddKeyView *joystickAddView;
@property (nonatomic, strong) KeyboardAddKeyView *keyboardAddView;
@property (nonatomic, strong) MouseAddKeyView *mouseAddView;

@property (weak, nonatomic) IBOutlet UIView *addBgView;
@property (weak, nonatomic) IBOutlet UIButton *switchKeyboardBtn;
@property (weak, nonatomic) IBOutlet UIButton *switchMouseBtn;

@property (weak, nonatomic) IBOutlet UIButton *hideBtn;

@property (weak, nonatomic) IBOutlet UITextField *nameTf;

@property (weak, nonatomic) IBOutlet UILabel *sizeLab;
@property (weak, nonatomic) IBOutlet UILabel *alphaLab;

@property (weak, nonatomic) IBOutlet UIButton *deleteBtn;
@property (weak, nonatomic) IBOutlet UIButton *onceBtn;
@property (weak, nonatomic) IBOutlet UIButton *longPressBtn;

// 按键交互View
@property (weak, nonatomic) IBOutlet UIView *clickTypeBgView;

// 按键名称View
@property (weak, nonatomic) IBOutlet UIView *nameBgView;

@property (nonatomic, strong) NSMutableArray<KeyModel *> *keyList;

@property (nonatomic, strong) KeyModel *currentM;

@end

@implementation CustomKeyViewController

- (NSMutableArray<KeyModel *> *)keyList {
    if (!_keyList) {
        _keyList = [NSMutableArray array];
    }

    return _keyList;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    [self configView];

    if (self.model.keyboard) {
        self.keyList = [self.model.keyboard mutableCopy];
    }

    self.keyView.keyList = self.keyList;



    [self configRac];
}

- (void)configView {
    self.switchMouseBtn.hidden = self.type == Custom_joystick;
    self.switchKeyboardBtn.hidden = self.type == Custom_joystick;

    if (self.type == Custom_joystick) {
        self.bottomCos.constant = k_JoystickHeightCos;
        self.addKeyViewHeightCos.constant = k_JoystickHeightCos;
        self.totalBottomHeight = k_JoystickHeightCos + k_HideBtnHeight;


        // 添加自定义手柄的view
        self.joystickAddView = [k_SanABundle loadNibNamed:@"JoystickAddKeyView" owner:self options:nil].lastObject;

        @weakify(self);
        self.joystickAddView.addCallback = ^(KeyModel *_Nonnull m) {
            @strongify(self);
            [self.keyView addKey:m];
        };

        [self.addBgView addSubview:self.joystickAddView];
        [self.joystickAddView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(@0);
        }];
    } else {
        self.bottomCos.constant = k_KeyboardHeightCos;
        self.addKeyViewHeightCos.constant = k_KeyboardHeightCos;
        self.totalBottomHeight = k_KeyboardHeightCos + k_HideBtnHeight;


        // 添加自定义键盘的view
        self.keyboardAddView = [k_SanABundle loadNibNamed:@"KeyboardAddKeyView" owner:self options:nil].lastObject;

        @weakify(self);
        self.keyboardAddView.addCallback = ^(KeyModel *_Nonnull m) {
            @strongify(self);
            [self.keyView addKey:m];
        };

        [self.addBgView addSubview:self.keyboardAddView];
        [self.keyboardAddView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(@0);
        }];

        // 添加自定义鼠标的view
        self.mouseAddView = [k_SanABundle loadNibNamed:@"MouseAddKeyView" owner:self options:nil].lastObject;

        self.mouseAddView.addCallback = ^(KeyModel *_Nonnull m) {
            @strongify(self);
            [self.keyView addKey:m];
        };

        self.mouseAddView.alpha = 0;

        [self.addBgView addSubview:self.mouseAddView];
        [self.mouseAddView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(@0);
        }];
    }

    self.bottomCos.constant = -self.totalBottomHeight;

    self.keyView = [[GameKeyContainerView alloc] initWithEdit:YES];
    @weakify(self);
    self.keyView.tapCallback = ^(KeyModel *_Nonnull m) {
        @strongify(self);
        self.currentM = m;
    };
    [self.view insertSubview:self.keyView atIndex:0];

    [self.keyView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(@0);
    }];

    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tap)];
    [self.view addGestureRecognizer:tap];
}

- (void)configRac {
    @weakify(self);
    [RACObserve(self, currentM) subscribeNext:^(id _Nullable x) {
        @strongify(self);
        [self configViewWithModel];
    }];

    [self.nameTf.rac_textSignal subscribeNext:^(NSString *_Nullable x) {
        @strongify(self);
        self.currentM.text = x;
    }];

    [[self.switchMouseBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        [UIView animateWithDuration:0.25
                         animations:^{
            @strongify(self);
            self.mouseAddView.alpha = 1;
            self.keyboardAddView.alpha = 0;

            self.switchMouseBtn.selected = YES;
            self.switchKeyboardBtn.selected = NO;
        }];
    }];

    [[self.switchKeyboardBtn rac_signalForControlEvents:UIControlEventTouchUpInside] subscribeNext:^(__kindof UIControl *_Nullable x) {
        [UIView animateWithDuration:0.25
                         animations:^{
            @strongify(self);
            self.mouseAddView.alpha = 0;
            self.keyboardAddView.alpha = 1;

            self.switchMouseBtn.selected = NO;
            self.switchKeyboardBtn.selected = YES;
        }];
    }];
}

- (void)configViewWithModel {
    self.nameTf.text = self.currentM.text.length ? self.currentM.text : @"";

    self.sizeLab.text = [NSString stringWithFormat:@"%ld%%", self.currentM.zoom];

    self.alphaLab.text = [NSString stringWithFormat:@"%ld%%", self.currentM.opacity];

    self.onceBtn.selected = self.currentM.click == 0;
    self.longPressBtn.selected = self.currentM.click == 1;


    self.nameBgView.hidden = self.currentM.key_type != KEY_kb_round;

    switch (self.currentM.key_type) {
        case KEY_mouse_left:
        case KEY_mouse_right:
        case KEY_mouse_wheel_center:
        case KEY_mouse_wheel_up:
        case KEY_mouse_wheel_down:
        case KEY_kb_round:{
            self.clickTypeBgView.hidden = NO;
        }
        break;

        default:
            self.clickTypeBgView.hidden = YES;
            break;
    }
}

- (void)tap {
    [self.view endEditing:YES];
}

/// MARK: 单击
- (IBAction)didTaponceClick:(id)sender {
    if (self.currentM) {
        self.currentM.click = 0;
        [self configViewWithModel];
    }
}

/// MARK: 长按
- (IBAction)didTapLongPress:(id)sender {
    if (self.currentM) {
        self.currentM.click = 1;
        [self configViewWithModel];
    }
}

/// MARK: 删除按钮
- (IBAction)didTapDelete:(id)sender {
    if (self.currentM) {
        [self.keyView removeKey:self.currentM];
        self.currentM = nil;
    }
}

/// MARK: 关闭
- (IBAction)didTapDismiss:(id)sender {
    [NormalAlertView showAlertWithTitle:@"退出编辑？"
                                content:@"会丢失当前所有改动！"
                           confirmTitle:@"退出"
                            cancelTitle:@"取消"
                        confirmCallback:^{
        [self dismissViewControllerAnimated:YES
                                 completion:^{
            if (self.dismissCallback) {
                self.dismissCallback(NO);
            }
        }];
    }
                         cancelCallback:nil];
}

/// MARK: 保存
- (IBAction)didTapSave:(id)sender {
    [self updateKeyList];
}

/// MARK: 调整大小
- (IBAction)didTapSizeDelAdd:(UIButton *)sender {
    if (self.currentM) {
        if (sender.tag == 10) {
            // 减
            if (self.currentM.zoom <= 10) {
                return;
            }

            self.currentM.zoom -= 10;
        }

        if (sender.tag == 20) {
            // 加

            if (self.currentM.zoom >= 100) {
                return;
            }

            self.currentM.zoom += 10;
        }

        [self configViewWithModel];
    }
}

/// MARK: 调整透明度
- (IBAction)didTapAlphaDelAdd:(UIButton *)sender {
    if (self.currentM) {
        if (sender.tag == 10) {
            // 减
            if (self.currentM.opacity <= 10) {
                return;
            }

            self.currentM.opacity -= 10;
        }

        if (sender.tag == 20) {
            // 加

            if (self.currentM.opacity >= 100) {
                return;
            }

            self.currentM.opacity += 10;
        }

        [self configViewWithModel];
    }
}

/// MARK: 还原按钮设置
- (IBAction)didTapReset:(id)sender {
    [NormalAlertView showAlertWithTitle:@"还原默认？"
                                content:@"会重置当前所有改动！"
                           confirmTitle:@"还原"
                            cancelTitle:@"取消"
                        confirmCallback:^{
        self.currentM = nil;
        [self resetKeyList];
    }
                         cancelCallback:nil];
}

/// MARK: 显示隐藏顶部菜单
- (IBAction)didTapShowHide:(id)sender {
    @weakify(self);
    [UIView animateWithDuration:0.25
                     animations:^{
        @strongify(self);

        self.topCos.constant = (self.topCos.constant == 0) ? -92 : 0;
        [self.hideBtn setImage:(self.topCos.constant == 0 ? k_BundleImage(@"custom_hide") : k_BundleImage(@"custom_show"))
                      forState:UIControlStateNormal];

        [self.view layoutIfNeeded];
    }];
}

/// MARK: 添加按钮
- (IBAction)didTapAddKey:(id)sender {
    @weakify(self);
    [UIView animateWithDuration:0.25
                     animations:^{
        @strongify(self);

        self.bottomCos.constant = (self.bottomCos.constant == 0) ? -self.totalBottomHeight : 0;


        [self.view layoutIfNeeded];
    }];
}

// MARK: 更新当前配置
- (void)updateKeyList {
    NSArray *keyboard = [self.keyList mapUsingBlock:^id _Nullable (KeyModel *_Nonnull obj, NSUInteger idx) {
        return [obj toJson];
    }];


    if (self.isEdit) {
        [[RequestTool share] requestUrl:k_api_updateKeyboard_custom_v2
                             methodType:Request_POST
                                 params:@{ @"use": @(self.model.use),
                                           @"id": self.model.ID,
                                           @"keyboard": keyboard }
                          faildCallBack:nil
                        successCallBack:^(id _Nonnull obj) {
            [self dismissViewControllerAnimated:YES
                                     completion:^{
                if (self.dismissCallback) {
                    self.dismissCallback(YES);
                }
            }];
        }];
    } else {
        [[RequestTool share] requestUrl:k_api_createKeyboard_custom_v2
                             methodType:Request_POST
                                 params:@{ @"type": self.type == Custom_joystick ? @"1" : @"2",
                                           @"game_id": [HmCloudTool share].gameId,
                                           @"keyboard": keyboard }
                          faildCallBack:nil
                        successCallBack:^(id _Nonnull obj) {
            [self dismissViewControllerAnimated:YES
                                     completion:^{
                if (self.dismissCallback) {
                    self.dismissCallback(YES);
                }
            }];
        }];
    }
}

// MARK: 还原默认
- (void)resetKeyList {
    self.keyList = [self.model.keyboard mutableCopy];
    self.keyView.keyList = self.keyList;
}

@end
