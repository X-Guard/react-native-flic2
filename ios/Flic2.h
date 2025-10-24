#import <Flic2Spec/Flic2Spec.h>
#import <flic2lib/FLICManager.h>
#import <flic2lib/FLICButton.h>
#import <flic2lib/FLICEnums.h>

@interface Flic2 : NativeFlic2SpecBase <NativeFlic2Spec, FLICManagerDelegate, FLICButtonDelegate>

@property (nonatomic, strong) NSMutableDictionary<NSString *, FLICButton *> *buttons;
@property (nonatomic, strong) FLICManager *manager;

@end
