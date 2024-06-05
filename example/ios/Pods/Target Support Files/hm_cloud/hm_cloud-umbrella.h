#ifdef __OBJC__
#import <UIKit/UIKit.h>
#else
#ifndef FOUNDATION_EXPORT
#if defined(__cplusplus)
#define FOUNDATION_EXPORT extern "C"
#else
#define FOUNDATION_EXPORT extern
#endif
#endif
#endif

#import "CloudPlayerWarpper.h"
#import "CloudPreViewController.h"
#import "HmCloudPlatformView.h"
#import "HmCloudPlugin.h"
#import "HmCloudView.h"
#import "HmCloudViewFactory.h"
#import "WMDragView.h"

FOUNDATION_EXPORT double hm_cloudVersionNumber;
FOUNDATION_EXPORT const unsigned char hm_cloudVersionString[];

