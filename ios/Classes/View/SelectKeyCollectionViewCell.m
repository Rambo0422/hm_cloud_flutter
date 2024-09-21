//
//  SelectKeyCollectionViewCell.m
//  hm_cloud
//
//  Created by a水 on 2024/9/14.
//

#import "SanA_Macro.h"
#import "SelectKeyCollectionViewCell.h"
@interface SelectKeyCollectionViewCell ()

@property (weak, nonatomic) IBOutlet UIButton *addBtn;
@property (weak, nonatomic) IBOutlet UIView *contentV;

@property (weak, nonatomic) IBOutlet UIButton *delBtn;
@property (weak, nonatomic) IBOutlet UIImageView *vipImg;
@property (weak, nonatomic) IBOutlet UIButton *editBtn;
@property (weak, nonatomic) IBOutlet UIButton *useBtn;
@property (weak, nonatomic) IBOutlet UIImageView *typeImg;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UIImageView *userAvatar;
@property (weak, nonatomic) IBOutlet UILabel *userName;

@end

@implementation SelectKeyCollectionViewCell

- (void)awakeFromNib {
    [super awakeFromNib];


    self.addBtn.layer.cornerRadius = 7;
    self.addBtn.layer.borderColor = kColor(0x282D36).CGColor;
    self.addBtn.layer.borderWidth = 1;

    self.contentV.layer.cornerRadius = 7;
}

- (void)setModel:(KeyDetailModel *)model {
    _model = model;

    self.contentV.hidden = (model == nil);
    self.addBtn.hidden = (model != nil);

    if (model) {
        // 官方没有删除按钮，没有编辑按钮，没有vip使用标志
        self.delBtn.hidden = model.isOfficial;
        self.editBtn.hidden = model.isOfficial;
        self.vipImg.hidden = model.isOfficial;


        self.nameLab.text = model.isOfficial ? (model.type == TypeJoystick ? @"官方手柄" : @"官方键鼠") : model.name;

        self.typeImg.image = (model.type == TypeJoystick) ? k_BundleImage(@"set_custom_joystick") : k_BundleImage(@"set_custom_keyboard");

        self.userAvatar.hidden = !model.isOfficial;
        self.userName.text = model.isOfficial ? @"官方分享" : @"用户分享";


        self.contentV.layer.borderColor = kColor(0xC6EC4B).CGColor;
        self.contentV.layer.borderWidth = model.use ? 1 : 0;

        [self.useBtn setTitle:(model.use ? @"使用中" : @"使用") forState:UIControlStateNormal];
        [self.useBtn setTitleColor:(model.use ? kColor(0xC6EC4B) : [UIColor whiteColor]) forState:UIControlStateNormal];
        self.useBtn.backgroundColor = model.use ? kColor(0x282F3A) : kColor(0x424A58);
    }
}

// MARK: 编辑
- (IBAction)didTapEdit:(id)sender {
    self.editCallback(self.model);
}

// MARK: 使用
- (IBAction)didTapUse:(id)sender {
    if (self.model.use) {
        return;
    }

    self.useCallback(self.model);
}

// MARK: 新增
- (IBAction)didTapAdd:(id)sender {
    self.addCallback(self.model);
}

// MARK: 删除
- (IBAction)didTapDelete:(id)sender {
    self.delCallback(self.model);
}

@end
