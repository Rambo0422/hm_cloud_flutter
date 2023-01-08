#import "AppDelegate.h"
#import "GeneratedPluginRegistrant.h"
#import <AVFAudio/AVFAudio.h>
@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  [GeneratedPluginRegistrant registerWithRegistry:self];
  // Override point for customization after application launch.
    [self setRouteNotifation];
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

-(void)setRouteNotifation{
    
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(routeNotification:) name:AVAudioSessionRouteChangeNotification object:[AVAudioSession sharedInstance]];
}

-(void)routeNotification:(NSNotification * )notification{
    // Determine hint type
    
    AVAudioSession * session = [AVAudioSession sharedInstance];
    NSLog(@"category = %@, mode = %@, options = %ld",session.category, session.mode, session.categoryOptions);
    
//    if (session.categoryOptions == 97) {
//        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord
//                                                mode:AVAudioSessionModeVoiceChat
//                                             options:(AVAudioSessionCategoryOptionMixWithOthers |
//                                                      AVAudioSessionCategoryOptionAllowBluetoothA2DP |
//                                                      AVAudioSessionCategoryOptionAllowAirPlay |
//                                                      AVAudioSessionCategoryOptionDefaultToSpeaker |
//                                                      AVAudioSessionCategoryOptionAllowBluetooth)
//                                               error:nil];
//    }
    
    
}

@end
