#import <Flic2Spec/Flic2Spec.h>
#import <flic2lib/FLICManager.h>
#import <flic2lib/FLICButton.h>
#import <flic2lib/FLICEnums.h>
#import <React/RCTBridgeModule.h>

@interface Flic2 : NativeFlic2SpecBase <NativeFlic2Spec, FLICManagerDelegate, FLICButtonDelegate>

@property (nonatomic, assign) BOOL managerRestored;

@end
