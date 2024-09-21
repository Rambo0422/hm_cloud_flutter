//
//  CustomSelectViewController.m
//  hm_cloud-SanA_Game
//
//  Created by a水 on 2024/8/9.
//

#import "CustomKeyViewController.h"
#import "CustomSelectViewController.h"

@interface CustomSelectViewController ()<UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout>

@property (nonatomic, strong) NSMutableArray<KeyDetailModel *> *joystickList;
@property (nonatomic, strong) NSMutableArray<KeyDetailModel *> *keyboardList;
@property (weak, nonatomic) IBOutlet UICollectionView *joystickCollectionView;
@property (weak, nonatomic) IBOutlet UICollectionView *keyboardCollectionView;

@end

@implementation CustomSelectViewController

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
    // Do any additional setup after loading the view from its nib.

    [self configView];

    [self getDefaultJoystickData];
    [self getDefaultKeyboardData];
}

- (void)configView {
    [self.joystickCollectionView registerNib:[UINib nibWithNibName:@"SelectKeyCollectionViewCell" bundle:k_SanABundle ] forCellWithReuseIdentifier:@"SelectKeyCollectionViewCell"];

    [self.keyboardCollectionView registerNib:[UINib nibWithNibName:@"SelectKeyCollectionViewCell" bundle:k_SanABundle ] forCellWithReuseIdentifier:@"SelectKeyCollectionViewCell"];
}

//MARK: 获取官方手柄
- (void)getDefaultJoystickData {
    [self.joystickList removeAllObjects];

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

        // 无默认手柄配置，取本地配置
        if (!error) {
            KeyDetailModel *normalJoystick = [[KeyDetailModel alloc] init];
            normalJoystick.keyboard = [KeyModel mj_objectArrayWithKeyValuesArray:arr];
            normalJoystick.isOfficial = YES;
            normalJoystick.type = TypeJoystick;
            [self.joystickList addObject:normalJoystick];

            [self getCustomJoystickData];
        }
    }
                    successCallBack:^(id _Nonnull obj) {
        // 有默认手柄 则正常处理，如果没有默认手柄 看上面faildCallback 👆🏻
        KeyDetailModel *normalJoystick = [KeyDetailModel mj_objectWithKeyValues:obj[@"data"]];
        normalJoystick.isOfficial = YES;
        normalJoystick.type = TypeJoystick;
        [self.joystickList addObject:normalJoystick];

        [self getCustomJoystickData];
    }];
}

//MARK: 获取官方键盘
- (void)getDefaultKeyboardData {
    [self.keyboardList removeAllObjects];

    [[RequestTool share] requestUrl:k_api_getKeyboard_v2
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        KeyDetailModel *normalKeyboard = [KeyDetailModel mj_objectWithKeyValues:obj[@"data"]];
        normalKeyboard.isOfficial = YES;
        normalKeyboard.type = TypeKeyboard;
        [self.keyboardList addObject:normalKeyboard];

        [self getCustomKeyboardData];
    }];
}

//MARK: 获取用户自定义手柄
- (void)getCustomJoystickData {
    [[RequestTool share] requestUrl:k_api_getKeyboard_custom_v2
                         methodType:Request_GET
                             params:@{ @"type": @"1", @"game_id": [HmCloudTool share].gameId, @"page": @"1", @"size": @"3" }
                      faildCallBack:^{
        KeyDetailModel *m = self.joystickList.firstObject;
        m.use = YES;
        [self.joystickCollectionView reloadData];
    }

                    successCallBack:^(id _Nonnull obj) {
        __block BOOL isUseOfficial = YES;

        NSArray *temp = [[KeyDetailModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"datas"]] mapUsingBlock:^id _Nullable (KeyDetailModel *_Nonnull obj, NSUInteger idx) {
            if (obj.name.length == 0) {
                obj.name = [NSString stringWithFormat:@"自定义手柄%ld", idx + 1];
            }

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

        [self.joystickCollectionView reloadData];
    }];
}

//MARK: 获取用户自定义键盘
- (void)getCustomKeyboardData {
    [[RequestTool share] requestUrl:k_api_getKeyboard_custom_v2
                         methodType:Request_GET
                             params:@{ @"type": @"2", @"game_id": [HmCloudTool share].gameId, @"page": @"1", @"size": @"3" }
                      faildCallBack:^{
        KeyDetailModel *m = self.keyboardList.firstObject;
        m.use = YES;
        [self.keyboardCollectionView reloadData];
    }
                    successCallBack:^(id _Nonnull obj) {
        __block BOOL isUseOfficial = YES;

        NSArray *temp = [[KeyDetailModel mj_objectArrayWithKeyValuesArray:obj[@"data"][@"datas"]] mapUsingBlock:^id _Nullable (KeyDetailModel *_Nonnull obj, NSUInteger idx) {
            if (obj.name.length == 0) {
                obj.name = [NSString stringWithFormat:@"自定义键盘%ld", idx + 1];
            }

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

        [self.keyboardCollectionView reloadData];
    }];
}

// MARK: 删除指定键盘
- (void)deleteKey:(KeyDetailModel *)m {
    [[RequestTool share] requestUrl:k_api_deleteKeyboard_custom_v2
                         methodType:Request_POST
                             params:@{ @"id": m.ID }
                      faildCallBack:nil
                    successCallBack:^(id _Nonnull obj) {
        if (m.type == TypeJoystick) {
            [self getDefaultJoystickData];
        } else {
            [self getDefaultKeyboardData];
        }
    }];
}

- (IBAction)didTapDismiss:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

// MARK: 使用指定键盘
- (void)useKey:(KeyDetailModel *)model {
    if (![HmCloudTool share].isVip) {
        [NormalAlertView showAlertWithTitle:nil
                                    content:nil
                               confirmTitle:nil
                                cancelTitle:nil
                            confirmCallback:^{
            [self dismissViewControllerAnimated:YES
                                     completion:^{
                self.pushVipCallback();
            }];
        }
                             cancelCallback:nil];

        return;
    }

    if (model.isOfficial) {
        __block KeyDetailModel *currentUseM = nil;


        NSArray *tempList = model.type == TypeJoystick ? self.joystickList : self.keyboardList;

        [tempList enumerateObjectsUsingBlock:^(KeyDetailModel *_Nonnull obj, NSUInteger idx, BOOL *_Nonnull stop) {
            if (obj.use) {
                currentUseM = obj;
                *stop = YES;
            }
        }];


        NSArray *keyboard = [currentUseM.keyboard mapUsingBlock:^id _Nullable (KeyModel *_Nonnull obj, NSUInteger idx) {
            return [obj toJson];
        }];


        [[RequestTool share] requestUrl:k_api_updateKeyboard_custom_v2
                             methodType:Request_POST
                                 params:@{ @"use": @0, @"keyboard": keyboard, @"id": currentUseM.ID }
                          faildCallBack:nil
                        successCallBack:^(id _Nonnull obj) {
        }];
    } else {
        NSArray *keyboard = [model.keyboard mapUsingBlock:^id _Nullable (KeyModel *_Nonnull obj, NSUInteger idx) {
            return [obj toJson];
        }];


        [[RequestTool share] requestUrl:k_api_updateKeyboard_custom_v2
                             methodType:Request_POST
                                 params:@{ @"use": @1, @"keyboard": keyboard, @"id": model.ID }
                          faildCallBack:nil
                        successCallBack:^(id _Nonnull obj) {
        }];
    }

    [self dismissViewControllerAnimated:YES
                             completion:^{
        self.useCallback(model);
    }];
}

// MARK: uicollectiondelegate

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return 4;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    SelectKeyCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"SelectKeyCollectionViewCell" forIndexPath:indexPath];

    @weakify(self);

    if (collectionView == self.joystickCollectionView) {
        if (indexPath.row >= self.joystickList.count) {
            cell.model = nil;
        } else {
            cell.model = self.joystickList[indexPath.row];
        }
    } else {
        if (indexPath.row >= self.keyboardList.count) {
            cell.model = nil;
        } else {
            cell.model = self.keyboardList[indexPath.row];
        }
    }

    cell.useCallback = ^(KeyDetailModel *_Nullable model) {
        @strongify(self);
        [self useKey:model];
    };

    cell.editCallback = ^(KeyDetailModel *_Nullable model) {
        @strongify(self);
        [self pushEditViewController:model
                                edit:YES];
    };

    cell.addCallback = ^(KeyDetailModel *_Nullable model) {
        @strongify(self);
        KeyDetailModel *tempM = nil;

        if (collectionView == self.joystickCollectionView) {
            tempM = self.joystickList.firstObject;
        } else {
            tempM = self.keyboardList.firstObject;
        }

        [self pushEditViewController:tempM edit:NO];
    };

    cell.delCallback = ^(KeyDetailModel *_Nullable model) {
        @strongify(self);
        [NormalAlertView showAlertWithTitle:@"删除按键吗?"
                                    content:@"删除后按键不可恢复"
                               confirmTitle:@"确认删除"
                                cancelTitle:@"取消"
                            confirmCallback:^{
            [self deleteKey:model];
        }
                             cancelCallback:nil];
    };

    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake((kScreenW - 120) / 2, 55);
}

// MARK: push edit View

- (void)pushEditViewController:(KeyDetailModel *)model edit:(BOOL)isEdit {
    CustomKeyViewController *vc = [[CustomKeyViewController alloc] initWithNibName:@"CustomKeyViewController"
                                                                            bundle:k_SanABundle];

    vc.modalPresentationStyle = UIModalPresentationCustom;
    vc.transitioningDelegate = self;
    vc.type = (model.type == TypeJoystick) ? Custom_joystick : Custom_keyboard;
    vc.model = model;
    vc.isEdit = isEdit;
    @weakify(self);
    vc.dismissCallback = ^(BOOL isRefresh) {
        @strongify(self);

        if (isRefresh) {
            if (model.type == TypeJoystick) {
                [self getDefaultJoystickData];
            } else {
                [self getDefaultKeyboardData];
            }
        }
    };
    [self presentViewController:vc
                       animated:YES
                     completion:nil];
}

@end
