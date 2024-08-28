//
//  NormalAlertView.m
//  hm_cloud
//
//  Created by a水 on 2024/8/15.
//

#import "NormalAlertView.h"
#import "SanA_Macro.h"

@interface NormalAlertView ()

@property (weak, nonatomic) IBOutlet UIView *contentView;


@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UILabel *contengLab;
@property (weak, nonatomic) IBOutlet UIButton *cancelBtn;
@property (weak, nonatomic) IBOutlet UIButton *confirmBtn;


@property (nonatomic, strong) void (^ confirm) (void);
@property (nonatomic, strong) void (^ cancel) (void);

@end

@implementation NormalAlertView

+ (void)showAlertWithTitle:(nullable NSString *)title
                   content:(nullable NSString *)content
              confirmTitle:(nullable NSString *)confirmTitle
               cancelTitle:(nullable NSString *)cancelTitle
           confirmCallback:(nullable void (^)(void))confirm
            cancelCallback:(nullable void (^)(void))cancel {
    NormalAlertView *alert = [k_SanABundle loadNibNamed:@"NormalAlertView"
                                                  owner:self
                                                options:nil].lastObject;


    alert.confirm = confirm;
    alert.cancel = cancel;

    if (title) {
        alert.titleLab.text = title;
    }

    if (content) {
        alert.contengLab.text = content;
    }

    if (confirmTitle) {
        [alert.confirmBtn setTitle:confirmTitle forState:UIControlStateNormal];
    }

    if (cancelTitle) {
        [alert.cancelBtn setTitle:cancelTitle forState:UIControlStateNormal];
    }

    KLCPopup *pop = [KLCPopup popupWithContentView:alert
                                          showType:KLCPopupShowTypeFadeIn
                                       dismissType:KLCPopupDismissTypeFadeOut
                                          maskType:KLCPopupMaskTypeDimmed
                          dismissOnBackgroundTouch:NO
                             dismissOnContentTouch:NO];

    [pop show];
}

- (IBAction)didTapConfirm:(id)sender {
    [KLCPopup dismissAllPopups];

    if (self.confirm) {
        self.confirm();
    }
}

- (IBAction)didTapCancel:(id)sender {
    [KLCPopup dismissAllPopups];

    if (self.cancel) {
        self.cancel();
    }
}

@end
