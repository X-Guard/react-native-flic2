#import "Flic2.h"
#import <flic2lib/flic2lib.h>

@implementation Flic2 {
    bool hasListeners;
}

+ (BOOL) requiresMainQueueSetup {
    return YES;
}

- (void) startObserving {
    hasListeners = YES;
}

- (void) stopObserving {
    hasListeners = NO;
}

- (void) sendEventMessage:(NSDictionary *)body {
    NSLog(@"FLIC2LIB EVENT %@", body);
    
    if (hasListeners) {
        [self sendEventWithName:@"FLIC2" body: body];
    }
}

- (void) sendEventScanMessage:(FLICButton *)button errorCode:(NSError *)error {
    NSLog(@"FLIC2LIB EVENT %@", @"scanResult");
    NSInteger errorCode;
    if(!error) {
        errorCode = 0;
    } else {
        errorCode = error.code;
    }
    if (hasListeners) {
        [self sendEventWithName:@"scanResult" body: @{
            @"error": @(errorCode == 0 ? false : true),
            @"result": @([self getCorrectScanResultCode:errorCode]),
            @"button": !button ? @{
                @"uuid": button.uuid,
                @"bluetoothAddress": button.bluetoothAddress,
                @"name": button.name,
                @"batteryLevel": @([self batteryVoltageToEstimatedPercentage:button.batterylevel]),
                @"voltage": @((button.batterylevel * 3.6) / 1024),
                @"isReady": @(button.isReady),
                @"isUnpaired": @(button.isUnpaired),
                @"pressCount": @(button.pressCount),
                @"firmwareRevision": @(button.firmwareRevision)
            } : [NSNull null],
        }];
    }
}

- (void) sendEventMessage:(NSString *)event button:(FLICButton *)button queued:(BOOL) queued age:(NSInteger) age {
    NSLog(@"FLIC2LIB EVENT %@", event);
    
    if (hasListeners) {
        [self sendEventWithName:event body: @{
        @"queued": @(queued),
        @"age": @(age),
        @"uuid": button.uuid,
        @"bluetoothAddress": button.bluetoothAddress,
        @"name": button.name,
        @"batteryLevel": @([self batteryVoltageToEstimatedPercentage:button.batterylevel]),
        @"voltage": @((button.batterylevel * 3.6) / 1024),
        @"isReady": @(button.isReady),
        @"isUnpaired": @(button.isUnpaired),
        @"pressCount": @(button.pressCount),
        @"firmwareRevision": @(button.firmwareRevision)
        }];
    }
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"FLIC2",
        @"scanResult",
        @"didReceiveButtonEvent",
        @"didReceiveButtonDown",
        @"didReceiveButtonUp",
        @"didReceiveButtonClick",
        @"didReceiveButtonDoubleClick",
        @"didReceiveButtonHold",
    ];
}

-(NSInteger) getCorrectScanResultCode: (NSInteger) code {

    switch (code) {
        case FLICButtonScannerErrorCodeBluetoothNotActivated:
            return 2;
            break;
        case FLICButtonScannerErrorCodeUnknown:
            return 3;
            break;
        case FLICButtonScannerErrorCodeNoPublicButtonDiscovered:
            return 4;
            break;
        case FLICButtonScannerErrorCodeAlreadyConnectedToAnotherDevice:
            return 5;
            break;
        case FLICButtonScannerErrorCodeConnectionTimeout:
            return 6;
            break;
        case FLICButtonScannerErrorCodeInvalidVerifier:
            return 7;
            break;
        case FLICButtonScannerErrorCodeBLEPairingFailedPreviousPairingAlreadyExisting:
            return 8;
            break;
        case FLICButtonScannerErrorCodeBLEPairingFailedUserCanceled:
            return 9;
            break;
        case FLICButtonScannerErrorCodeBLEPairingFailedUnknownReason:
            return 10;
            break;
        case FLICButtonScannerErrorCodeAppCredentialsDontMatch:
            return 11;
            break;
        case FLICButtonScannerErrorCodeUserCanceled:
            return 12;
            break;
        case FLICButtonScannerErrorCodeInvalidBluetoothAddress:
            return 13;
            break;
        case FLICButtonScannerErrorCodeGenuineCheckFailed:
            return 14;
            break;
        case FLICButtonScannerErrorCodeTooManyApps:
            return 15;
            break;
        case FLICButtonScannerErrorCodeCouldNotSetBluetoothNotify:
            return 16;
            break;
        case FLICButtonScannerErrorCodeCouldNotDiscoverBluetoothServices:
            return 17;
            break;
        case FLICButtonScannerErrorCodeButtonDisconnectedDuringVerification:
            return 18;
            break;
        case FLICButtonScannerErrorCodeFailedToEstablish:
            return 19;
            break;
        case FLICButtonScannerErrorCodeConnectionLimitReached:
            return 20;
            break;
        case FLICButtonScannerErrorCodeNotInPublicMode:
            return 21;
            break;
        default:
            return 3;
            break;
    }

}

-(NSInteger) batteryVoltageToEstimatedPercentage: (NSInteger)voltage {
    NSInteger mvolt = (NSInteger)(((voltage * 3.6) / 1024) * 1000);
    NSInteger percentage;
    if (mvolt >= 3000) {
        percentage = 100;
    } else if (mvolt >= 2900) {
        percentage = 42 + (mvolt - 2900) * 58 / 100;
    } else if (mvolt >= 2740) {
        percentage = 18 + (mvolt - 2740) * 24 / 160;
    } else if (mvolt >= 2440) {
        percentage = 6 + (mvolt - 2440) * 12 / 300;
    } else if (mvolt >= 2100) {
        percentage = (mvolt - 2100) * 6 / 340;
    } else {
        percentage = 0;
    }
    return percentage;
}

-(NSDictionary *) convertButtonToDict: (FLICButton *) button
{
        NSLog(@"FLIC2LIB convertButtonToDict %@", button.name);
    
    return @{
             @"uuid": button.uuid,
             @"bluetoothAddress": button.bluetoothAddress,
             @"name": button.name,
             @"batteryLevel": @([self batteryVoltageToEstimatedPercentage:button.batterylevel]),
             @"voltage": @((button.batterylevel * 3.6) / 1024),
             @"isReady": @(button.isReady),
             @"isUnpaired": @(button.isUnpaired),
             @"pressCount": @(button.pressCount),
             @"firmwareRevision": @(button.firmwareRevision)
             };
}


// -- SCLFlicManagerDelegate --

RCT_EXPORT_METHOD(startup) {
    [FLICManager configureWithDelegate:self buttonDelegate:self background:YES];
    [self sendEventMessage: @{@"event": @"initFlicManager"}];
}

RCT_EXPORT_METHOD(getButtons:(RCTResponseSenderBlock)callback callback:(RCTResponseSenderBlock)calback) {
    NSMutableArray *buttonArray = [[NSMutableArray alloc] init];
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;
}

RCT_EXPORT_METHOD(connectAllKnownButtons) {
    [self sendEventMessage: @{@"event": @"connectAllKnownButtons"}];

    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        NSLog(@"Flic2 Connect button: %@", button.name);
        button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
        [button disconnect];
        [button connect];
    }
}

RCT_EXPORT_METHOD(startScan) {
    
    [self scanForButton];
}

RCT_EXPORT_METHOD(stopScanning) {
    [self stopScan];
}

RCT_EXPORT_METHOD(forgetAllButtons) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        [button disconnect];
        [[FLICManager sharedManager] forgetButton:(button) completion:^(NSUUID * _Nonnull uuid, NSError * _Nullable error) {
        }];
    }
}

RCT_EXPORT_METHOD(forgetButton:(NSString *) uuid) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        if ([button.uuid isEqualToString:uuid]) {
            [button disconnect];
            [[FLICManager sharedManager] forgetButton:(button) completion:^(NSUUID * _Nonnull uuid, NSError * _Nullable error) {
                [self sendEventMessage: @{@"event": @"forgetButton"}];
            }];
            break;
        }
    }
}

- (void)stopScan {

    [[FLICManager sharedManager] stopScan];
    [self sendEventMessage: @{@"event": @"stopScan"}];
}

- (void)scanForButton;
{
    [self sendEventMessage: @{@"event": @"startScan"}];
        NSLog(@"A Flic2 scanForButton");
    [[FLICManager sharedManager] scanForButtonsWithStateChangeHandler:^(FLICButtonScannerStatusEvent event) {
        
        NSLog(@"A Flic2 scanForButtonsWithStateChangeHandler %ld", (long)event);
        // You can use these events to update your UI.
        switch (event)
        {
            case FLICButtonScannerStatusEventDiscovered:
                NSLog(@"A Flic2 was discovered.");
                break;
            case FLICButtonScannerStatusEventConnected:
                NSLog(@"A Flic2 is being verified.");
                break;
            case FLICButtonScannerStatusEventVerified:
                NSLog(@"The Flic2 was verified successfully.");
                break;
            case FLICButtonScannerStatusEventVerificationFailed:
                NSLog(@"The Flic2 verification failed.");
                break;
            default:
                break;
        }
    } completion:^(FLICButton *button, NSError *error) {
        NSLog(@"Flic2 Scanner completed with error: %@", error);
        if (!error)
        {
            NSLog(@"Flic2 Successfully verified: %@, %@, %@", button.name, button.bluetoothAddress, button.serialNumber);
            // Listen to single click only.
            button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
            [button connect];
            
            [self sendEventScanMessage: (button) errorCode:(0)];
        } else {
            [self sendEventScanMessage: (button) errorCode:(error)];
        }
    }];
}

- (void)managerDidRestoreState:(FLICManager *)manager;
{
    // The mager was restored and can now be used.
    for (FLICButton *button in manager.buttons)
    {
        NSLog(@"Did restore Flic2: %@", button.name);
    }
}

- (void)manager:(nonnull FLICManager *)manager didUpdateBluetoothState:(CBManagerState)state {
    NSLog(@"Flic2 Update bluetooth state: %ld", (long)state);
    [self sendEventMessage: @{@"event": @"didUpdateBluetoothState", @"state": @((long)state)}];
}


// -- SCLFlicButtonDelegate --

- (void)buttonDidConnect:(FLICButton *)button;
{
    NSLog(@"Did connect Flic2: %@", button.name);
    [self sendEventMessage: @{
        @"event": @"buttonConnectionCompleted",
        @"name": button.name,
    }];
}

- (void)button:(FLICButton *)button didDisconnectWithError:(NSError *)error;
{
    NSLog(@"Did disconnect Flic2: %@", error);
    [self sendEventMessage: @{
        @"event": @"buttonDisconnected",
        @"name": button.name,
        @"error": error
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonDown:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic2: %@ was clicked down", button.name);
    [self sendEventMessage: (@"didReceiveButtonDown") button:(button) queued:(queued) age:(age)];
}

- (void)button:(FLICButton *)button didReceiveButtonUp:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic2: %@ was clicked up", button.name);
    [self sendEventMessage: (@"didReceiveButtonUp") button:(button) queued:(queued) age:(age)];
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic2: %@ was clicked", button.name);
    [self sendEventMessage: (@"didReceiveButtonClick") button:(button) queued:(queued) age:(age)];
}

- (void)button:(FLICButton *)button didReceiveButtonDoubleClick:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic2: %@ was clicked double", button.name);
    [self sendEventMessage: (@"didReceiveButtonDoubleClick") button:(button) queued:(queued) age:(age)];
}

- (void)button:(FLICButton *)button didReceiveButtonHold:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic2: %@ was hold", button.name);
    [self sendEventMessage: (@"didReceiveButtonHold") button:(button) queued:(queued) age:(age)];
}

- (void)button:(FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error;
{
    NSLog(@"Did fail to connect Flic2: %@", error);
    [self sendEventMessage: @{
        @"event": @"buttonConnectionFailure",
        @"name": button.name,
        @"error": error
    }];
}

- (void)button:(FLICButton *)button didUnpairWithError:(NSError * _Nullable)error;
{
    NSLog(@"Did unpair with error Flic2: %@", error);
    [self sendEventMessage: @{
        @"event": @"buttonConnectionUnpaired",
        @"name": button.name,
        @"error": error
    }];
}

- (void)buttonIsReady:(nonnull FLICButton *)button {
     NSLog(@"Flic2: %@ is ready", button.name);
    [self sendEventMessage: @{
        @"event": @"buttonConnectionReady",
        @"name": button.name,
    }];
}


@end
