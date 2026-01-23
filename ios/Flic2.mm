#import "Flic2.h"
#import <React/RCTBridgeModule.h>

@implementation Flic2

- (instancetype)init {
    self = [super init];
    return self;
}

// MARK: - FLICManager Methods

- (void)initialize:(BOOL)background
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    // Configure the shared manager (this is the correct way)
    FLICManager *manager = [FLICManager configureWithDelegate:self buttonDelegate:self background:background];

    if (manager) {
        resolve(nil);
    } else {
        reject(@"INIT_ERROR", @"Failed to initialize FLICManager", nil);
    }
}

- (void)getButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    NSArray<FLICButton *> *buttons = [[FLICManager sharedManager] buttons];
    NSMutableArray *buttonDicts = [[NSMutableArray alloc] init];

    for (FLICButton *button in buttons) {
        [buttonDicts addObject:[self buttonToDictionary:button]];
    }

    resolve(buttonDicts);
}

- (void)scanForButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    if (!self.managerRestored) {
        reject(@"NOT_RESTORED", @"Manager not restored yet. Wait for managerDidRestoreState", nil);
        return;
    }

    NSLog(@"Starting scan");

    __weak Flic2 *weakSelf = self;

    // Emit started event
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakSelf emitOnScanStatusChange:@{
            @"event": @"started",
            @"eventName": @"started"
        }];
    });

    [[FLICManager sharedManager] scanForButtonsWithStateChangeHandler:^(FLICButtonScannerStatusEvent event) {

        // Intermediate scan status events are intentionally not emitted

    } completion:^(FLICButton * _Nullable button, NSError * _Nullable error) {
        NSLog(@"Scan completion called - button: %@, error: %@", button ? @"YES" : @"NO", error);

        NSInteger resultCode = [weakSelf mapScanErrorToResultCode:error];

        if (error) {
            NSLog(@"Scan error: %@ (code: %ld, mapped: %ld)", error.localizedDescription, (long)error.code, (long)resultCode);
        } else if (button) {
            NSLog(@"Button found: %@", button.uuid);

            // Set trigger mode and auto-connect like old implementation
            button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
            [button connect];

            // Emit button event for discovered button
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf emitOnButtonEvent:@{
                    @"uuid": button.uuid,
                    @"event": @"discovered",
                    @"button": [weakSelf buttonToDictionary:button]
                }];
            });

        } else {
            NSLog(@"No button found and no error");
        }

        // Emit scan completion with result code
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf emitOnScanStatusChange:@{
                @"event": @"completion",
                @"eventName": @"completion",
                @"result": @(resultCode)
            }];
        });
    }];

    // Return immediately - scan results will come through events
    resolve(nil);
}

- (void)stopScan:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    NSLog(@"Stopping scan");
    [[FLICManager sharedManager] stopScan];

    resolve(nil);
}

- (void)forgetButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    FLICButton *button = [self findButtonByUUID:uuid];

    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    // Disconnect before forgetting like old implementation
    [button disconnect];

    [[FLICManager sharedManager] forgetButton:button completion:^(NSUUID *uuid, NSError * _Nullable error) {
        if (error) {
            reject(@"FORGET_ERROR", error.localizedDescription, error);
        } else {
            resolve(nil);
        }
    }];
}

// MARK: - FLICButton Methods

- (void)connectButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self findButtonByUUID:uuid];

    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    [button connect];
    resolve([self buttonToDictionary:button]);
}

- (void)disconnectButton:(NSString *)uuid
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self findButtonByUUID:uuid];

    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    [button disconnect];
    resolve([self buttonToDictionary:button]);
}

- (void)setTriggerMode:(NSString *)uuid mode:(NSInteger)mode
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self findButtonByUUID:uuid];

    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    button.triggerMode = (FLICButtonTriggerMode)mode;
    resolve([self buttonToDictionary:button]);
}

- (void)setLatencyMode:(NSString *)uuid mode:(NSInteger)mode
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self findButtonByUUID:uuid];

    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    button.latencyMode = (FLICLatencyMode)mode;
    resolve([self buttonToDictionary:button]);
}

- (void)setNickname:(NSString *)uuid nickname:(NSString *)nickname
    resolve:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    FLICButton *button = [self findButtonByUUID:uuid];

    if (!button) {
        reject(@"BUTTON_NOT_FOUND", @"Button not found", nil);
        return;
    }

    button.nickname = nickname;
    resolve([self buttonToDictionary:button]);
}

// MARK: - Helper Methods

- (void)connectAllKnownButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    NSArray<FLICButton *> *buttons = [[FLICManager sharedManager] buttons];

    for (FLICButton *button in buttons) {
        NSLog(@"Flic2 Connect button: %@", button.name);
        button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
        [button connect];
    }

    resolve(nil);
}

- (void)disconnectAllKnownButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    NSArray<FLICButton *> *buttons = [[FLICManager sharedManager] buttons];

    for (FLICButton *button in buttons) {
        NSLog(@"Flic2 disconnect button: %@", button.name);
        [button disconnect];
    }

    resolve(nil);
}

- (void)forgetAllButtons:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    NSArray<FLICButton *> *buttons = [[FLICManager sharedManager] buttons];

    for (FLICButton *button in buttons) {
        [button disconnect];
        [[FLICManager sharedManager] forgetButton:button completion:^(NSUUID *uuid, NSError * _Nullable error) {
            // Individual completion handlers not needed for bulk operation
        }];
    }

    resolve(nil);
}

- (void)isScanning:(RCTPromiseResolveBlock)resolve
    reject:(RCTPromiseRejectBlock)reject
{
    if (![FLICManager sharedManager]) {
        reject(@"NOT_INITIALIZED", @"Manager not initialized", nil);
        return;
    }

    BOOL scanning = [[FLICManager sharedManager] isScanning];
    resolve(@(scanning));
}

// MARK: - FLICManagerDelegate

- (void)managerDidRestoreState:(FLICManager *)manager {
    // Only emit restored event if we haven't already done so
    if (!self.managerRestored) {
        self.managerRestored = YES;
        NSLog(@"Manager state restored - ready for operations");
        dispatch_async(dispatch_get_main_queue(), ^{
            [self emitOnManagerStateChange:@{
                @"event": @"restored",
                @"state": @(manager.state),
                @"stateName": [self managerStateToString:manager.state],
                @"message": @"Manager state restored"
            }];
        });
    }
}

- (void)manager:(FLICManager *)manager didUpdateState:(FLICManagerState)state {
    // Always emit stateChanged for every state change
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnManagerStateChange:@{
            @"state": @(state),
            @"stateName": [self managerStateToString:state],
            @"event": @"stateChanged"
        }];
    });

    // Additionally emit restored event when manager becomes powered on (if not already restored)
    // This ensures the event fires on every app launch, not just during state restoration
    if (state == FLICManagerStatePoweredOn && !self.managerRestored) {
        self.managerRestored = YES;
        NSLog(@"Manager powered on - ready for operations");
        dispatch_async(dispatch_get_main_queue(), ^{
            [self emitOnManagerStateChange:@{
                @"event": @"restored",
                @"state": @(state),
                @"stateName": [self managerStateToString:state],
                @"message": @"Manager ready"
            }];
        });
    }
}

// MARK: - FLICButtonDelegate

- (void)buttonDidConnect:(FLICButton *)button {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"connected",
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)buttonIsReady:(FLICButton *)button {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"ready",
            @"button": [self buttonToDictionary:button]
        }];
    });
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

    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:eventData];
    });
}

- (void)button:(FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"connectionFailed",
            @"error": @{
                @"code": @(error.code),
                @"message": error.localizedDescription
            },
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didReceiveButtonDown:(BOOL)queued age:(NSInteger)age {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"buttonDown",
            @"queued": @(queued),
            @"age": @(age),
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didReceiveButtonUp:(BOOL)queued age:(NSInteger)age {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"buttonUp",
            @"queued": @(queued),
            @"age": @(age),
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"click",
            @"queued": @(queued),
            @"age": @(age),
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didReceiveButtonDoubleClick:(BOOL)queued age:(NSInteger)age {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"doubleClick",
            @"queued": @(queued),
            @"age": @(age),
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didReceiveButtonHold:(BOOL)queued age:(NSInteger)age {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"hold",
            @"queued": @(queued),
            @"age": @(age),
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didUnpairWithError:(NSError * _Nullable)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"unpaired",
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didUpdateBatteryVoltage:(float)voltage {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"batteryUpdate",
            @"voltage": @(voltage),
            @"button": [self buttonToDictionary:button]
        }];
    });
}

- (void)button:(FLICButton *)button didUpdateNickname:(NSString *)nickname {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self emitOnButtonEvent:@{
            @"uuid": button.uuid,
            @"event": @"nicknameUpdate",
            @"nickname": nickname,
            @"button": [self buttonToDictionary:button]
        }];
    });
}

// MARK: - Helper Methods

- (FLICButton *)findButtonByUUID:(NSString *)uuid {
    NSArray<FLICButton *> *buttons = [[FLICManager sharedManager] buttons];
    for (FLICButton *button in buttons) {
        if ([button.uuid isEqualToString:uuid]) {
            return button;
        }
    }
    return nil;
}

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

- (NSInteger)mapScanErrorToResultCode:(NSError *)error {
    if (!error) {
        return 0; // SUCCESS
    }

    switch (error.code) {
        case FLICButtonScannerErrorCodeBluetoothNotActivated:
            return 2; // BLUETOOTH_NOT_ACTIVATED
        case FLICButtonScannerErrorCodeUnknown:
            return 3; // UNKNOWN
        case FLICButtonScannerErrorCodeNoPublicButtonDiscovered:
            return 4; // NO_PUBLIC_BUTTON_DISCOVERED
        case FLICButtonScannerErrorCodeAlreadyConnectedToAnotherDevice:
            return 5; // ALREADY_CONNECTED_TO_ANOTHER_DEVICE
        case FLICButtonScannerErrorCodeConnectionTimeout:
            return 6; // CONNECTION_TIMEOUT
        case FLICButtonScannerErrorCodeInvalidVerifier:
            return 7; // INVALID_VERIFIER
        case FLICButtonScannerErrorCodeBLEPairingFailedPreviousPairingAlreadyExisting:
            return 8; // BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING
        case FLICButtonScannerErrorCodeBLEPairingFailedUserCanceled:
            return 9; // BLE_PAIRING_FAILED_USER_CANCELED
        case FLICButtonScannerErrorCodeBLEPairingFailedUnknownReason:
            return 10; // BLE_PAIRING_FAILED_UNKNOWN_REASON
        case FLICButtonScannerErrorCodeAppCredentialsDontMatch:
            return 11; // APP_CREDENTIALS_DONT_MATCH
        case FLICButtonScannerErrorCodeUserCanceled:
            return 12; // USER_CANCELED
        case FLICButtonScannerErrorCodeInvalidBluetoothAddress:
            return 13; // INVALID_BLUETOOTH_ADDRESS
        case FLICButtonScannerErrorCodeGenuineCheckFailed:
            return 14; // GENUINE_CHECK_FAILED
        case FLICButtonScannerErrorCodeTooManyApps:
            return 15; // TOO_MANY_APPS
        case FLICButtonScannerErrorCodeCouldNotSetBluetoothNotify:
            return 16; // COULD_NOT_SET_BLUETOOTH_NOTIFY
        case FLICButtonScannerErrorCodeCouldNotDiscoverBluetoothServices:
            return 17; // COULD_NOT_DISCOVER_BLUETOOTH_SERVICES
        case FLICButtonScannerErrorCodeButtonDisconnectedDuringVerification:
            return 18; // BUTTON_DISCONNECTED_DURING_VERIFICATION
        case FLICButtonScannerErrorCodeFailedToEstablish:
            return 19; // FAILED_TO_ESTABLISH
        case FLICButtonScannerErrorCodeConnectionLimitReached:
            return 20; // CONNECTION_LIMIT_REACHED
        case FLICButtonScannerErrorCodeNotInPublicMode:
            return 21; // NOT_IN_PUBLIC_MODE
        default:
            return 3; // UNKNOWN
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
