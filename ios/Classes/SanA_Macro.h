//
//  SanA_Macro.h
//  Pods
//
//  Created by a水 on 2024/8/7.
//

#ifndef SanA_Macro_h
#define SanA_Macro_h

#import <AFNetworking/AFNetworking.h>
#import <AVFAudio/AVFAudio.h>
#import <Masonry/Masonry.h>
#import <MJExtension/MJExtension.h>
#import <ReactiveObjC/ReactiveObjC.h>

#define k_changeSound       @"changeSound"
#define k_startSuccess      @"startSuccess"
#define k_startFailed       @"startFailed"
#define k_cloudInitBegan    @"cloudInitBegan"
#define k_cloudQueueInfo    @"cloudQueueInfo"
#define k_videoVisble       @"videoVisble"
#define k_videoFailed       @"videoFailed"


#define k_DelayInfo         @"delayInfo"
#define k_GameStop          @"gameStop"
#define k_FirstFrameArrival @"firstFrameArrival"

#define k_SanABundle        [NSBundle bundleWithPath:[[NSBundle bundleForClass:self.class] pathForResource:@"SanA_Game" ofType:@"bundle"]]
#define k_BundleImage(name) [UIImage imageNamed:name inBundle:k_SanABundle withConfiguration:nil]


#define kScreenW            [UIScreen mainScreen].bounds.size.width
#define kScreenH            [UIScreen mainScreen].bounds.size.height

#define kColor(RGB)         [UIColor colorWithRed:((RGB >> 16) & 0x00FF) / 255. green:((RGB >> 8) & 0x00FF) / 255. blue:((RGB >> 0) & 0x00FF) / 255. alpha:1]

#endif /* SanA_Macro_h */
