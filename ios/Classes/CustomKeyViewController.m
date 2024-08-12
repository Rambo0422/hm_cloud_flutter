//
//  CustomKeyViewController.m
//  hm_cloud
//
//  Created by a水 on 2024/8/9.
//

#import "CustomKeyViewController.h"
#import "GameKeyView.h"

@interface CustomKeyViewController ()

@property (nonatomic, strong) GameKeyView *keyView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topCos;
@property (weak, nonatomic) IBOutlet UIButton *hideBtn;
@property (weak, nonatomic) IBOutlet UITextField *nameTf;
@property (weak, nonatomic) IBOutlet UILabel *sizeLab;
@property (weak, nonatomic) IBOutlet UILabel *alphaLab;

@property (nonatomic, strong) NSArray<KeyModel *> *keyList;

@property (nonatomic, strong) KeyModel *currentM;

@end

@implementation CustomKeyViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    [self configView];

    [self request];

    [self configRac];
}

- (void)request {
    [self requestKeyList];
}

- (void)requestKeyList {
    if (self.type == Custom_joystick) {
        [self getJoystick];
    } else {
        [self getKeyboard];
    }
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
}

- (void)configViewWithModel {
    self.nameTf.text = self.currentM.text.length ? self.currentM.text : @"";

    self.sizeLab.text = [NSString stringWithFormat:@"%ld%%", self.currentM.zoom];

    self.alphaLab.text = [NSString stringWithFormat:@"%ld%%", self.currentM.opacity];
}

- (void)configView {
    self.keyView = [[GameKeyView alloc] initWithEdit:YES];
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

- (void)tap {
    [self.view endEditing:YES];
}

- (IBAction)didTapDismiss:(id)sender {
    [self dismissViewControllerAnimated:YES
                             completion:^{
        if (self.dismissCallback) {
            self.dismissCallback(NO);
        }
    }];
}

- (IBAction)didTapSave:(id)sender {
    [self dismissViewControllerAnimated:YES
                             completion:^{
        if (self.dismissCallback) {
            self.dismissCallback(YES);
        }
    }];
}

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

- (IBAction)didTapReset:(id)sender {
    self.currentM = nil;
    [self requestKeyList];
}

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

- (void)getJoystick {
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
            self.keyList = [KeyModel mj_objectArrayWithKeyValuesArray:arr];
            self.keyView.keyList = self.keyList;
        }
    }
                    successCallBack:^(id _Nonnull obj) {
        self.keyList = [KeyModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"keyboard"]];
        self.keyView.keyList = self.keyList;
    }];
}

- (void)getKeyboard {
    [[RequestTool share] requestUrl:k_api_getKeyboard
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        self.keyList = [KeyModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"keyboard"]];
        self.keyView.keyList = self.keyList;
    }];
}

/*
 #pragma mark - Navigation

   // In a storyboard-based application, you will often want to do a little preparation before navigation
   - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
   }
 */

@end
