//
//  PartyAvatarCollectionViewCell.m
//  hm_cloud
//
//  Created by a水 on 2024/10/30.
//

#import "PartyAvatarCollectionViewCell.h"
#import "SanA_Macro.h"

@interface PartyAvatarCollectionViewCell ()

@property (weak, nonatomic) IBOutlet UIImageView *img;
@property (weak, nonatomic) IBOutlet UIImageView *permissionImg;


@end

@implementation PartyAvatarCollectionViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.img.layer.borderColor = [[UIColor whiteColor] colorWithAlphaComponent:0.8].CGColor;
}

- (void)setModel:(PartyAvatarModel *)model {
    _model = model;
    self.permissionImg.hidden = !model.isPermission;

    if (model.uid.length) {
        self.img.contentMode = UIViewContentModeScaleAspectFill;
        // 异步下载图片
        NSURLSessionDataTask *downloadTask = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:model.avatar_url]
                                                                         completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            if (error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                                   self.img.image = k_BundleImage(@"avatar_normal");
                               });

                return;
            }

            if (data) {
                UIImage *image = [UIImage imageWithData:data];

                // 在主线程更新 UI
                dispatch_async(dispatch_get_main_queue(), ^{
                                   if (image) {
                                       self.img.image = image;
                                   } else {
                                       self.img.image = k_BundleImage(@"avatar_normal");
                                   }
                               });
            }
        }];

        [downloadTask resume];     // 启动任务
    } else {
        self.img.image = model.status == 1 ? k_BundleImage(@"party_unlock") : k_BundleImage(@"party_lock");
        self.img.contentMode = UIViewContentModeCenter;
    }
}

@end
