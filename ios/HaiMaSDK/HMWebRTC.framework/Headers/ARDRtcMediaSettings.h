#import <Foundation/Foundation.h>

#import "RTCMacros.h"

typedef NS_ENUM(NSInteger, RTC_OBJC_TYPE(ARDRtcCaptureMode)) {
  kARDRtcCaptureModeNone,
  kARDRtcCaptureModeInternalAudioVideo,
  kARDRtcCaptureModeExternalAudioInternalVideo,
  kARDRtcCaptureModeVideoOnly,
  kARDRtcCaptureModeInternalAudioOnly,
  kARDRtcCaptureModeExternalAudioOnly,
};

RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDRtcMicSettings) : NSObject
@property (nonatomic, assign) int samplerate;
@property (nonatomic, assign) int channels;
+ (instancetype) instanceWithMicSettings:(int)samplerate channels:(int)channels;
@end

RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDRtcCameraSettings) : NSObject
@property (nonatomic, assign) int width;
@property (nonatomic, assign) int height;
@property (nonatomic, assign) int framerate;
@property (nonatomic, assign) bool usefrontcamera;
@property (nonatomic, assign) bool isportrait;
+ (instancetype) instanceWithCameraSettings:(int)width height:(int)height framerate:(int)framerate usefrontcamera:(bool)usefrontcamera;
@end

RTC_OBJC_EXPORT
@interface RTC_OBJC_TYPE (ARDRtcVideoEncoderSettings) : NSObject
@property (nonatomic, assign) int maxbitrate;
@property (nonatomic, assign) int minbitrate;
+ (instancetype) instanceWithEncoderSettings:(int)minbitrate maxbitrate:(int)maxbitrate;
@end
