//
//  HmCloudView.m
//  hm_cloud
//
//  Created by 周智水 on 2023/1/4.
//

#import "HmCloudView.h"

@interface InterceptView : UIView

@end

@implementation InterceptView

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{}

- (void)touchesCancelled:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{}

@end

@interface HmCloudView ()

@property (nonatomic, strong) InterceptView * _view;

@end

@implementation HmCloudView

- (instancetype)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if (self) {
     
        self._view = [[InterceptView alloc] initWithFrame:CGRectZero];
        [self addSubview:self._view];
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

- (void)layoutSubviews{
    [super layoutSubviews];
    
    // 获取到正确的frame
    self._view.frame = self.bounds;
}

@end
