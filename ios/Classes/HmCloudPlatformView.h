//
//  HmCloudPlatformView.h
//  hm_cloud
//
//  Created by 周智水 on 2023/1/3.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
NS_ASSUME_NONNULL_BEGIN

@interface HmCloudPlatformView : NSObject<FlutterPlatformView>

- (id)initWithFrame:(CGRect)frame
             viewId:(int64_t)viewId
               args:(id)args
           messager:(NSObject<FlutterBinaryMessenger>*)messenger;

@end

NS_ASSUME_NONNULL_END
