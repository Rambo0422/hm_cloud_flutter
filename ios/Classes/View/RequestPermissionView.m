//
//  RequestPermissionView.m
//  hm_cloud
//
//  Created by a水 on 2024/11/6.
//

#import "RequestPermissionView.h"
#import "SanA_Macro.h"
@interface RequestPermissionView ()

@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *avatarImgView;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *nameLab;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *countdownLab;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *agreeBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *closeBtn;


@property (nonatomic, strong) KLCPopup *pop;

@property (nonatomic, strong) NSTimer *timer;

@property (nonatomic, strong) NSString *requestUid;

@end

@implementation RequestPermissionView

+ (instancetype)share {
    static RequestPermissionView *requestV;

    static dispatch_once_t token;

    dispatch_once(&token, ^{
        requestV = [k_SanABundle loadNibNamed:@"RequestPermissionView" owner:self options:nil].lastObject;

        requestV.agreeBtn.layer.borderColor = kColor(0xC6EC4B).CGColor;
        requestV.agreeBtn.layer.borderWidth = 0.5;
        requestV.agreeBtn.layer.cornerRadius = 13;

        requestV.frame = CGRectMake(0, 0, 380, 40);
    });

    return requestV;
}

- (void)showRequest:(NSDictionary *)dict inView:(UIView *)view {
    [self.pop dismiss:YES];

    if (self.timer) {
        [self.timer invalidate];
        self.timer = nil;
    }

    [self configView:dict];

    self.pop = [KLCPopup popupWithContentView:self showType:KLCPopupShowTypeSlideInFromTop dismissType:KLCPopupDismissTypeSlideOutToTop maskType:KLCPopupMaskTypeNone dismissOnBackgroundTouch:NO dismissOnContentTouch:NO];
    [self.pop showAtCenter:CGPointMake(kScreenW / 2, 20) inView:view];
}

- (void)configView:(NSDictionary *)dict {
    NSString *avatar = dict[@"avatar"];

    NSString *nickName = dict[@"nickName"];

    self.requestUid = dict[@"uid"];

    self.avatarImgView.contentMode = UIViewContentModeScaleAspectFill;
    // 异步下载图片
    NSURLSessionDataTask *downloadTask = [[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:avatar]
                                                                     completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                               self.avatarImgView.image = k_BundleImage(@"avatar_normal");
                           });

            return;
        }

        if (data) {
            UIImage *image = [UIImage imageWithData:data];

            // 在主线程更新 UI
            dispatch_async(dispatch_get_main_queue(), ^{
                               if (image) {
                                   self.avatarImgView.image = image;
                               } else {
                                   self.avatarImgView.image = k_BundleImage(@"avatar_normal");
                               }
                           });
        }
    }];

    [downloadTask resume];

    self.nameLab.text = nickName.length ? nickName : self.requestUid;

    __block NSInteger t = 60;

    @weakify(self);
    self.timer = [NSTimer scheduledTimerWithTimeInterval:1
                                                 repeats:YES
                                                   block:^(NSTimer *_Nonnull timer) {
        @strongify(self);
        t--;

        if (t == 0) {
            [self.timer invalidate];
            self.timer = nil;
            [self.pop dismiss:YES];
        }

        self.countdownLab.text = [NSString stringWithFormat:@"申请游戏控制权(%lds)", t];
    }];
}

- (IBAction)didTapAgree:(id)sender {
    [self.pop dismiss:YES];

    if (self.letPlayCallback) {
        self.letPlayCallback(self.requestUid);
    }
}

- (IBAction)didTapClose:(id)sender {
    [self.pop dismiss:YES];
}

@end
