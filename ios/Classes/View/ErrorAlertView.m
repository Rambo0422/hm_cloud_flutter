//
//  ErrorAlertView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/15.
//

#import "ErrorAlertView.h"
#import "SanA_Macro.h"

@interface ErrorAlertView ()

@property (weak, nonatomic) IBOutlet UIView *contentView;

@property (weak, nonatomic) IBOutlet UILabel *cidLab;
@property (weak, nonatomic) IBOutlet UILabel *uidLab;
@property (weak, nonatomic) IBOutlet UILabel *errorCodeLab;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UILabel *contengLab;

@property (nonatomic, strong) void (^ callback) (void);

@end



@implementation ErrorAlertView

- (void)awakeFromNib {
    [super awakeFromNib];
    self.contentView.layer.borderColor = kColor(0x44495B).CGColor;
    self.contentView.layer.borderWidth = 1;
    self.contentView.layer.cornerRadius = 10;
}

+ (void)showAlertWithCid:(nullable NSString *)cid uid:(nullable NSString *)uid errorCode:(nullable NSString *)errorCode title:(nullable NSString *)title content:(nullable NSString *)content dissMissCallback:(nullable void (^)(void))callback {
    ErrorAlertView *alert = [k_SanABundle loadNibNamed:@"ErrorAlertView"
                                                 owner:self
                                               options:nil].lastObject;

    alert.cidLab.text = [NSString stringWithFormat:@"CID：%@", cid];
    alert.uidLab.text = [NSString stringWithFormat:@"UID：%@", uid];
    alert.errorCodeLab.text = [NSString stringWithFormat:@"错误码：%@", errorCode];
    alert.callback = callback;

    if (title) {
        alert.titleLab.text = title;
    }

    if (content) {
        alert.contengLab.text = content;
    }

    KLCPopup *pop = [KLCPopup popupWithContentView:alert
                                          showType:KLCPopupShowTypeFadeIn
                                       dismissType:KLCPopupDismissTypeFadeOut
                                          maskType:KLCPopupMaskTypeDimmed
                          dismissOnBackgroundTouch:NO
                             dismissOnContentTouch:NO];

    [pop show];
}

- (IBAction)didTapAction:(id)sender {
    [KLCPopup dismissAllPopups];

    if (self.callback) {
        self.callback();
    }
}

/*
   // Only override drawRect: if you perform custom drawing.
   // An empty implementation adversely affects performance during animation.
   - (void)drawRect:(CGRect)rect {
    // Drawing code
   }
 */

@end
