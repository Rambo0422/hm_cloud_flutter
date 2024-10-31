//
//  PartyAvatarCollectionViewCell.m
//  hm_cloud
//
//  Created by a水 on 2024/10/30.
//

#import "PartyAvatarCollectionViewCell.h"

@interface PartyAvatarCollectionViewCell ()

@property (weak, nonatomic) IBOutlet UIImageView *img;


@end

@implementation PartyAvatarCollectionViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.img.layer.borderColor = [[UIColor whiteColor] colorWithAlphaComponent:0.8].CGColor;
}

@end
