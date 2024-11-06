//
//  PartyAvatarTableViewCell.m
//  hm_cloud
//
//  Created by a水 on 2024/11/1.
//

#import "PartyAvatarTableViewCell.h"
#import "SanA_Macro.h"
@interface PartyAvatarTableViewCell ()

@property (weak, nonatomic) IBOutlet UIImageView *avatarImg;
@property (weak, nonatomic) IBOutlet UILabel *ownerLab;
@property (weak, nonatomic) IBOutlet UIImageView *permissionImg;
@property (weak, nonatomic) IBOutlet UILabel *indexLab;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UIButton *playBtn;
@property (weak, nonatomic) IBOutlet UIButton *kicoutBtn;
@property (weak, nonatomic) IBOutlet UIStackView *stackView;


@end

@implementation PartyAvatarTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.avatarImg.layer.borderColor = [[UIColor whiteColor] colorWithAlphaComponent:0.5].CGColor;
    self.avatarImg.layer.borderWidth = 1.5;
    self.indexLab.layer.masksToBounds = YES;
}

- (void)configViewWithModel:(PartyAvatarModel *)model index:(NSInteger)index {
    self.model = model;

    self.permissionImg.hidden = !model.isPermission;

    self.indexLab.text = [NSString stringWithFormat:@"%ldP", index + 1];
    self.nameLab.text = model.nickname.length ? model.nickname : (model.uid.length ? [model.uid stringByReplacingCharactersInRange:NSMakeRange(0, 10) withString:@"***"] : @"虚位以待");

    self.ownerLab.hidden = (index != 0);

    self.nameLab.textColor = [[HmCloudTool share].userId isEqualToString:model.uid] ? kColor(0xC6EC4B) : UIColor.whiteColor;

    self.stackView.hidden = YES;
    self.kicoutBtn.hidden = NO;

    if ([HmCloudTool share].isAudience) {
        if ([[HmCloudTool share].userId isEqualToString:model.uid] && !model.isPermission) {
            self.stackView.hidden = NO;
        }

        self.kicoutBtn.hidden = YES;

        [self.playBtn setTitle:@"让我玩" forState:UIControlStateNormal];
        self.playBtn.backgroundColor = kColor(0xC6EC4B);
        [self.playBtn setTitleColor:kColor(0x222222) forState:UIControlStateNormal];
    }

    if ([HmCloudTool share].isAnchor) {
        self.stackView.hidden = NO;

        if ([[HmCloudTool share].userId isEqual:model.uid]) {
            self.kicoutBtn.hidden = YES;

            [self.playBtn setTitle:self.model.isPermission ? @"不让玩" : @"我要玩" forState:UIControlStateNormal];
            self.playBtn.backgroundColor = kColor(0xC6EC4B);

            [self.playBtn setTitleColor:kColor(0x222222) forState:UIControlStateNormal];
        } else {
            if (model.uid.length) {
                [self.playBtn setTitle:self.model.isPermission ? @"不让玩" : @"让Ta玩" forState:UIControlStateNormal];

                self.playBtn.backgroundColor = kColor(0xC6EC4B);
                [self.playBtn setTitleColor:kColor(0x222222) forState:UIControlStateNormal];
            } else {
                self.kicoutBtn.hidden = YES;

                [self.playBtn setTitle:(model.status == 1) ? @"锁定位置" : @"打开位置" forState:UIControlStateNormal];

                self.playBtn.backgroundColor = kColor(0x222A3A);

                [self.playBtn setTitleColor:kColor(0x8995A9) forState:UIControlStateNormal];
            }
        }
    }

    if (model.uid.length) {
        self.avatarImg.contentMode = UIViewContentModeScaleAspectFill;
        // 异步下载图片
        NSURLSessionDataTask *downloadTask = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:model.avatar_url]
                                                                         completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            if (error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                                   self.avatarImg.image = k_BundleImage(@"avatar_normal");
                               });

                return;
            }

            if (data) {
                UIImage *image = [UIImage imageWithData:data];

                // 在主线程更新 UI
                dispatch_async(dispatch_get_main_queue(), ^{
                                   if (image) {
                                       self.avatarImg.image = image;
                                   } else {
                                       self.avatarImg.image = k_BundleImage(@"avatar_normal");
                                   }
                               });
            }
        }];

        [downloadTask resume];     // 启动任务
    } else {
        self.avatarImg.image = model.status == 1 ? k_BundleImage(@"party_unlock") : k_BundleImage(@"party_lock");
        self.avatarImg.contentMode = UIViewContentModeCenter;
    }
}

- (IBAction)didTapPlayBtn:(id)sender {
    // 房主
    if ([HmCloudTool share].isAnchor) {
        if (self.model.isPermission) {
            // 有权限 - 不让玩
            if (self.closeUserPlayCallback) {
                self.closeUserPlayCallback(self.model.uid);
            }
        } else {
            // 有权限 - 让他玩
            if (self.letPlayCallback) {
                self.letPlayCallback(self.model.uid);
            }
        }
    }

    // 从控
    if ([HmCloudTool share].isAudience) {
        // 让我玩

        if (self.wantPlayCallback) {
            self.wantPlayCallback(self.model.uid);
        }
    }
}

- (IBAction)didTapKickoutBtn:(id)sender {
    if (self.kickoutCallback) {
        self.kickoutCallback(self.model.uid);
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
