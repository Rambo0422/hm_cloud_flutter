/*
 *  Copyright 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#import <Foundation/Foundation.h>
#if TARGET_OS_IPHONE
#import <UIKit/UIKit.h>
#endif

#import "RTCMacros.h"

NS_ASSUME_NONNULL_BEGIN

@class RTC_OBJC_TYPE(RTCVideoFrame);

RTC_OBJC_EXPORT
@protocol RTC_OBJC_TYPE
(RTCVideoRenderer) <NSObject>

    /** The size of the frame. */
    - (void)setSize : (CGSize)size;

/** The frame to be displayed. */
- (void)renderFrame:(nullable RTC_OBJC_TYPE(RTCVideoFrame) *)frame;

/// reset firstRenderFlat to false
- (void)resetFirstRenderFlag;

/// Enable or Disable FrameRendered callback
/// @param enabled YES or NO
- (void)enableFrameRenderedCallback:(BOOL)enabled;

/// CurrentImage
- (UIImage *)getCurrentImage:(BOOL)is_portrait;

/// Render fps, following up android func name.
- (int)getEglRenderFps;
@end

RTC_OBJC_EXPORT
@protocol RTC_OBJC_TYPE
(RTCVideoViewDelegate)  <NSObject>

    - (void)videoView : (id<RTC_OBJC_TYPE(RTCVideoRenderer)>)videoView didChangeVideoSize
    : (CGSize)size;
- (void)videoView:(id<RTC_OBJC_TYPE(RTCVideoRenderer)>)videoView didRenderFirstFrameAt:(int64_t)timestampNS;
- (void)videoView:(id<RTC_OBJC_TYPE(RTCVideoRenderer)>)videoView didRenderFrameAt:(int64_t)timestamp;

@end

NS_ASSUME_NONNULL_END
