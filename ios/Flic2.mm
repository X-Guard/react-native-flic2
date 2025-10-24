#import "Flic2.h"

@implementation Flic2

- (instancetype)init {
    self = [super init];
    if (self) {
        _buttons = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (NSNumber *)multiply:(double)a b:(double)b {

    NSLog(@"multiply: %f * %f", a, b);
    NSNumber *result = @(a * b);

    [self emitOnMultiply:@{@"a": @(a), @"b": @(b), @"result": result}];

    return result;
}

// MARK: - FLICManager Methods

- (void)initialize:(BOOL)background
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (self.manager) {
        resolve(@{@"success": @YES, @"message": @"Manager already initialized"});
        return;
    }

    self.manager = [FLICManager configureWithDelegate:self buttonDelegate:self background:background];

    if (self.manager) {
        resolve(@{@"success": @YES, @"message": @"Manager initialized successfully"});
    } else {
        reject(@"INIT_ERROR", @"Failed to initialize FLICManager", nil);
    }
}

- (void)getButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (!self.manager) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    NSArray<FLICButton *> *buttons = [self.manager buttons];
    NSMutableArray *buttonDicts = [[NSMutableArray alloc] init];

    for (FLICButton *button in buttons) {
        [buttonDicts addObject:[self buttonToDictionary:button]];
    }

    resolve(buttonDicts);
}

- (void)scanForButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (!self.manager) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    if (self.manager.isScanning) {
        reject(@"ALREADY_SCANNING", @"Scan already in progress", nil);
        return;
    }

    [self.manager scanForButtonsWithStateChangeHandler:^(FLICButtonScannerStatusEvent event) {
        [self emitOnScanStatusChange:@{
            @"event": @(event),
            @"eventName": [self scannerEventToString:event]
        }];
    } completion:^(FLICButton * _Nullable button, NSError * _Nullable error) {
        if (error) {
            reject(@"SCAN_ERROR", error.localizedDescription, error);
        } else if (button) {
            [self.buttons setObject:button forKey:button.uuid];
            button.delegate = self;
            resolve([self buttonToDictionary:button]);
        } else {
            reject(@"SCAN_ERROR", @"No button found", nil);
        }
    }];
}

- (void)stopScan:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (!self.manager) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    [self.manager stopScan];
    resolve(@{@"success": @YES, @"message": @"Scan stopped"});
}

- (void)forgetButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (!self.manager) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    FLICButton *button = [self.buttons objectForKey:uuid];
    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    [self.manager forgetButton:button completion:^(NSUUID *uuid, NSError * _Nullable error) {
        if (error) {
            reject(@"FORGET_ERROR", error.localizedDescription, error);
        } else {
            [self.buttons removeObjectForKey:uuid.UUIDString];
            resolve(@{@"success": @YES, @"message": @"Button forgotten"});
        }
    }];
}

// MARK: - FLICButton Methods

- (void)connectButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self.buttons objectForKey:uuid];
    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    [button connect];
    resolve(@{@"success": @YES, @"message": @"Connection initiated"});
}

- (void)disconnectButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self.buttons objectForKey:uuid];
    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    [button disconnect];
    resolve(@{@"success": @YES, @"message": @"Disconnection initiated"});
}

- (void)setTriggerMode:(NSString *)uuid mode:(NSInteger)mode
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self.buttons objectForKey:uuid];
    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    button.triggerMode = (FLICButtonTriggerMode)mode;
    resolve(@{@"success": @YES, @"message": @"Trigger mode set"});
}

- (void)setLatencyMode:(NSString *)uuid mode:(NSInteger)mode
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self.buttons objectForKey:uuid];
    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    button.latencyMode = (FLICLatencyMode)mode;
    resolve(@{@"success": @YES, @"message": @"Latency mode set"});
}

- (void)setNickname:(NSString *)uuid nickname:(NSString *)nickname
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self.buttons objectForKey:uuid];
    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    button.nickname = nickname;
    resolve(@{@"success": @YES, @"message": @"Nickname set"});
}

// MARK: - FLICManagerDelegate

- (void)managerDidRestoreState:(FLICManager *)manager {
    [self emitOnManagerStateChange:@{
        @"event": @"restored",
        @"message": @"Manager state restored"
    }];
}

- (void)manager:(FLICManager *)manager didUpdateState:(FLICManagerState)state {
    [self emitOnManagerStateChange:@{
        @"state": @(state),
        @"stateName": [self managerStateToString:state],
        @"event": @"stateChanged"
    }];
}

// MARK: - FLICButtonDelegate

- (void)buttonDidConnect:(FLICButton *)button {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"connected",
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)buttonIsReady:(FLICButton *)button {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"ready",
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didDisconnectWithError:(NSError * _Nullable)error {
    NSMutableDictionary *eventData = [NSMutableDictionary dictionaryWithDictionary:@{
        @"uuid": button.uuid,
        @"event": @"disconnected",
        @"button": [self buttonToDictionary:button]
    }];

    if (error) {
        eventData[@"error"] = @{
            @"code": @(error.code),
            @"message": error.localizedDescription
        };
    }

    [self emitOnButtonEvent:eventData];
}

- (void)button:(FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"connectionFailed",
        @"error": @{
            @"code": @(error.code),
            @"message": error.localizedDescription
        },
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonDown:(BOOL)queued age:(NSInteger)age {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"buttonDown",
        @"queued": @(queued),
        @"age": @(age),
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonUp:(BOOL)queued age:(NSInteger)age {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"buttonUp",
        @"queued": @(queued),
        @"age": @(age),
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"click",
        @"queued": @(queued),
        @"age": @(age),
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonDoubleClick:(BOOL)queued age:(NSInteger)age {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"doubleClick",
        @"queued": @(queued),
        @"age": @(age),
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonHold:(BOOL)queued age:(NSInteger)age {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"hold",
        @"queued": @(queued),
        @"age": @(age),
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didUnpairWithError:(NSError * _Nullable)error {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"unpaired",
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didUpdateBatteryVoltage:(float)voltage {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"batteryUpdate",
        @"voltage": @(voltage),
        @"button": [self buttonToDictionary:button]
    }];
}

- (void)button:(FLICButton *)button didUpdateNickname:(NSString *)nickname {
    [self emitOnButtonEvent:@{
        @"uuid": button.uuid,
        @"event": @"nicknameUpdate",
        @"nickname": nickname,
        @"button": [self buttonToDictionary:button]
    }];
}

// MARK: - Helper Methods

- (NSDictionary *)buttonToDictionary:(FLICButton *)button {
    return @{
        @"uuid": button.uuid,
        @"identifier": button.identifier.UUIDString,
        @"name": button.name ?: @"",
        @"nickname": button.nickname ?: @"",
        @"bluetoothAddress": button.bluetoothAddress,
        @"serialNumber": button.serialNumber,
        @"state": @(button.state),
        @"stateName": [self buttonStateToString:button.state],
        @"triggerMode": @(button.triggerMode),
        @"triggerModeName": [self triggerModeToString:button.triggerMode],
        @"latencyMode": @(button.latencyMode),
        @"latencyModeName": [self latencyModeToString:button.latencyMode],
        @"pressCount": @(button.pressCount),
        @"firmwareRevision": @(button.firmwareRevision),
        @"isReady": @(button.isReady),
        @"batteryVoltage": @(button.batteryVoltage),
        @"isUnpaired": @(button.isUnpaired)
    };
}

- (NSString *)managerStateToString:(FLICManagerState)state {
    switch (state) {
        case FLICManagerStateUnknown:
            return @"unknown";
        case FLICManagerStateResetting:
            return @"resetting";
        case FLICManagerStateUnsupported:
            return @"unsupported";
        case FLICManagerStateUnauthorized:
            return @"unauthorized";
        case FLICManagerStatePoweredOff:
            return @"poweredOff";
        case FLICManagerStatePoweredOn:
            return @"poweredOn";
        default:
            return @"unknown";
    }
}

- (NSString *)buttonStateToString:(FLICButtonState)state {
    switch (state) {
        case FLICButtonStateDisconnected:
            return @"disconnected";
        case FLICButtonStateConnecting:
            return @"connecting";
        case FLICButtonStateConnected:
            return @"connected";
        case FLICButtonStateDisconnecting:
            return @"disconnecting";
        default:
            return @"unknown";
    }
}

- (NSString *)triggerModeToString:(FLICButtonTriggerMode)mode {
    switch (mode) {
        case FLICButtonTriggerModeClickAndHold:
            return @"clickAndHold";
        case FLICButtonTriggerModeClickAndDoubleClick:
            return @"clickAndDoubleClick";
        case FLICButtonTriggerModeClickAndDoubleClickAndHold:
            return @"clickAndDoubleClickAndHold";
        case FLICButtonTriggerModeClick:
            return @"click";
        default:
            return @"unknown";
    }
}

- (NSString *)latencyModeToString:(FLICLatencyMode)mode {
    switch (mode) {
        case FLICLatencyModeNormal:
            return @"normal";
        case FLICLatencyModeLow:
            return @"low";
        default:
            return @"unknown";
    }
}

- (NSString *)scannerEventToString:(FLICButtonScannerStatusEvent)event {
    switch (event) {
        case FLICButtonScannerStatusEventDiscovered:
            return @"discovered";
        case FLICButtonScannerStatusEventConnected:
            return @"connected";
        case FLICButtonScannerStatusEventVerified:
            return @"verified";
        case FLICButtonScannerStatusEventVerificationFailed:
            return @"verificationFailed";
        default:
            return @"unknown";
    }
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
