//
//  ToastAlertView.m
//  hm_cloud
//
//  Created by a水 on 2024/9/22.
//

#import "SanA_Macro.h"
#import "ToastAlertView.h"

@interface ToastAlertView ()
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UILabel *contentLab;

@end

@implementation ToastAlertView

+ (void)showAlertWithTitle:(nullable NSString *)title
                   content:(nullable NSString *)content {
    ToastAlertView *alert = [k_SanABundle loadNibNamed:@"ToastAlertView"
                                                 owner:self
                                               options:nil].lastObject;




    if (title) {
        alert.titleLab.text = title;
    }

    if (content) {
        alert.contentLab.text = content;
    }

    KLCPopup *pop = [KLCPopup popupWithContentView:alert
                                          showType:KLCPopupShowTypeFadeIn
                                       dismissType:KLCPopupDismissTypeFadeOut
                                          maskType:KLCPopupMaskTypeDimmed
                          dismissOnBackgroundTouch:NO
                             dismissOnContentTouch:NO];

    [pop showWithDuration:2.5];
}

@end
