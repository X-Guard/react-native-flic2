#if __has_include(<React/RCTBridgeModule.h>)
  #import <React/RCTBridgeModule.h>
#else
  #import "RCTBridgeModule.h"
#endif

#import <React/RCTEventEmitter.h>
@import flic2lib;

@interface Flic2 : RCTEventEmitter <FLICButtonDelegate, FLICManagerDelegate, RCTBridgeModule>
@end
