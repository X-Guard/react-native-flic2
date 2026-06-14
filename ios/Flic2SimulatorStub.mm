#import "Flic2SimulatorStub.h"
#import <React/RCTBridgeModule.h>

@implementation Flic2

- (void)initialize:(BOOL)background
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)getButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(@[]);
}

- (void)scanForButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)stopScan:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)forgetButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)connectAllKnownButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)disconnectAllKnownButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)forgetAllButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(nil);
}

- (void)isScanning:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve(@(NO));
}

- (void)connectButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve([self emptyButtonDictionary:uuid]);
}

- (void)disconnectButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve([self emptyButtonDictionary:uuid]);
}

- (void)setTriggerMode:(NSString *)uuid mode:(double)mode
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve([self emptyButtonDictionary:uuid]);
}

- (void)setLatencyMode:(NSString *)uuid mode:(double)mode
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    resolve([self emptyButtonDictionary:uuid]);
}

- (void)setNickname:(NSString *)uuid nickname:(NSString *)nickname
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    NSMutableDictionary *button = [[self emptyButtonDictionary:uuid] mutableCopy];
    button[@"nickname"] = nickname ?: @"";
    resolve(button);
}

- (NSDictionary *)emptyButtonDictionary:(NSString *)uuid
{
    NSString *safeUuid = uuid ?: @"";
    return @{
        @"uuid": safeUuid,
        @"identifier": safeUuid,
        @"name": @"",
        @"nickname": @"",
        @"bluetoothAddress": @"",
        @"serialNumber": @"",
        @"state": @(0),
        @"stateName": @"disconnected",
        @"triggerMode": @(0),
        @"triggerModeName": @"clickAndHold",
        @"latencyMode": @(0),
        @"latencyModeName": @"normal",
        @"pressCount": @(0),
        @"firmwareRevision": @(0),
        @"isReady": @(NO),
        @"batteryVoltage": @(0),
        @"isUnpaired": @(NO)
    };
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeFlic2SpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"Flic2";
}

@end
