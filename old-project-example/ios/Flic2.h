
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
@import flic2lib;

@interface Flic2 : RCTEventEmitter <FLICButtonDelegate, FLICManagerDelegate, RCTBridgeModule>
@end
