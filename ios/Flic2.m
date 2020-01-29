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

- (void) sendEventScanMessage:(FLICButton *)button event:(NSString *) event errorCode:(NSInteger) error {
    NSLog(@"FLIC2LIB sendEventScanMessage %@", event);
    Boolean errorBool;
    if(error == 0) {
        errorBool = false;
    } else {
        errorBool = true;
    }

    if (hasListeners) {
        if (button != nil) {
            [self sendEventWithName:@"scanResult" body: @{
                @"event": event,
                @"error": @(errorBool),
                @"result": @([self getCorrectScanResultCode:error]),
                @"button": [self convertButtonToDictForScan:button]
            }];
        } else {
            [self sendEventWithName:@"scanResult" body: @{
                @"event": event,
                @"error": @(errorBool),
                @"result": @([self getCorrectScanResultCode:error]),
            }];
        }

    }
}

- (void) sendEventScanStatusMessage:(NSString *)event  {
    NSLog(@"FLIC2LIB sendEventScanStatusMessage %@", event);
    if (hasListeners) {

        [self sendEventWithName:@"scanResult" body: @{
            @"event": event,
        }];
        
    }
}

- (void) sendEventMessage:(NSString *)event button:(FLICButton *)button errorCode:(NSInteger) error {
    NSLog(@"FLIC2LIB EVENT button error %@", event);
    
    if (hasListeners) {
        [self sendEventWithName:@"didReceiveButtonEvent" body: @{
        @"event": event,
        @"error": @(error),
        @"button": [self convertButtonToDictForScan:button]
        }];
    }
}

- (void) sendEventMessage:(NSString *)event button:(FLICButton *)button  {
    NSLog(@"FLIC2LIB EVENT only button %@", event);
    
    if (hasListeners) {
        [self sendEventWithName:@"didReceiveButtonEvent" body: @{
        @"event": event,
        @"button": [self convertButtonToDictForScan:button]
        }];
    }
}

- (void) sendEventMessage:(NSString *)event button:(FLICButton *)button queued:(BOOL) queued age:(NSInteger) age {
    NSLog(@"FLIC2LIB EVENT button queed age %@", event);
    
    if (hasListeners) {
        [self sendEventWithName:@"didReceiveButtonEvent" body: @{
        @"event": event,
        @"queued": @(queued),
        @"age": @(age),
        @"button": [self convertButtonToDictForScan:button]
        }];
    }
}

-(NSDictionary *) convertButtonToDictForScan: (FLICButton *) button
{
    NSLog(@"FLIC2LIB convertButtonToDictForScan %@ %@ %@ %@ %f %@ %u %@ %@", button.name, button.uuid, button.bluetoothAddress, button.nickname, button.batteryVoltage,@(button.pressCount),button.firmwareRevision, button.serialNumber, @([self batteryVoltageToEstimatedPercentage:button.batteryVoltage]));
    
    if (button != nil) {
        return @{
                 @"uuid": button.uuid,
                 @"bluetoothAddress": button.bluetoothAddress,
                 @"name": button.nickname == nil ? button.name : button.nickname,
                 @"batteryLevelIsOk": @([self batteryVoltageToEstimatedPercentage:button.batteryVoltage]),
                 @"voltage": @(button.batteryVoltage),
                 @"isReady": @(button.isReady),
                 @"isUnpaired": @(button.isUnpaired),
                 @"pressCount": @(button.pressCount),
                 @"firmwareRevision": @(button.firmwareRevision),
                 @"serial": button.serialNumber
                 };
    } else {
        return false;
    }

}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"managerInitialized",
        @"scanResult",
        @"didReceiveButtonEvent",
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

-(BOOL) batteryVoltageToEstimatedPercentage: (float)voltage {
    float mvolt = (float)(voltage * 1000);

    if (mvolt >= 2650) {
        return true;
    } else {
        return false;
    }
}

-(NSDictionary *) convertButtonToDict: (FLICButton *) button
{
    NSLog(@"FLIC2LIB convertButtonToDict %@ %@ %@ %@ %f %@ %u %@ %@", button.name, button.uuid, button.bluetoothAddress, button.nickname, button.batteryVoltage,@(button.pressCount),button.firmwareRevision, button.serialNumber, @([self batteryVoltageToEstimatedPercentage:button.batteryVoltage]));
    
    return @{
             @"uuid": button.uuid,
             @"bluetoothAddress": button.bluetoothAddress,
             @"name": button.nickname == nil ? button.name : button.nickname,
             @"batteryLevelIsOk": @([self batteryVoltageToEstimatedPercentage:button.batteryVoltage]),
             @"voltage": @(button.batteryVoltage),
             @"isReady": @(button.isReady),
             @"isUnpaired": @(button.isUnpaired),
             @"pressCount": @(button.pressCount),
             @"firmwareRevision": @(button.firmwareRevision),
             @"serial": button.serialNumber
             };
}


// -- SCLFlicManagerDelegate --

RCT_EXPORT_METHOD(startup) {
    [FLICManager configureWithDelegate:self buttonDelegate:self background:YES];
}

RCT_EXPORT_METHOD(getButtons:(RCTResponseSenderBlock)callback callback:(RCTResponseSenderBlock)calback) {
    
   NSMutableArray *buttonArray = [[NSMutableArray alloc] init];
   NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;
   
   for (FLICButton *button in buttons) {
           NSLog(@"FLIC2LIB getButtons %@ %@ %@ %@ %f %@ %u %@ %@", button.name, button.uuid, button.bluetoothAddress, button.nickname, button.batteryVoltage,@(button.pressCount),button.firmwareRevision, button.serialNumber, @([self batteryVoltageToEstimatedPercentage:button.batteryVoltage]));
     [buttonArray addObject: [self convertButtonToDict: button]];
   }
   
   callback(@[buttonArray]);
   
}

RCT_EXPORT_METHOD(connectButton:(NSString *) uuid callback:(RCTResponseSenderBlock) successCallBack) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        if ([button.uuid isEqualToString:uuid]) {
            [button connect];
            successCallBack(@[]);
            break;
        }
    }
    
    return;

}

RCT_EXPORT_METHOD(connectAllKnownButtons) {

    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        NSLog(@"Flic2 Connect button: %@", button.name);
        button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
        [button connect];
    }
}

RCT_EXPORT_METHOD(disconnectAllKnownButtons) {

    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        NSLog(@"Flic2 diconnect button: %@", button.name);
        [button disconnect];
    }
}

RCT_EXPORT_METHOD(disconnectButton:(NSString *) uuid callback:(RCTResponseSenderBlock) successCallBack) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        if ([button.uuid isEqualToString:uuid]) {
            [button disconnect];
            successCallBack(@[]);
            break;
        }
    }
    
    return;

}

RCT_EXPORT_METHOD(startScan) {
    
    [self scanForButton];
}

RCT_EXPORT_METHOD(stopScan) {
    [self stopScanForButton];
}

RCT_EXPORT_METHOD(forgetAllButtons) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        [button disconnect];
        [[FLICManager sharedManager] forgetButton:(button) completion:^(NSUUID * _Nonnull uuid, NSError * _Nullable error) {
        }];
    }
}

RCT_EXPORT_METHOD(forgetButton:(NSString *) uuid callback:(RCTResponseSenderBlock) successCallBack) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        if ([button.uuid isEqualToString:uuid]) {
            [button disconnect];
            [[FLICManager sharedManager] forgetButton:(button) completion:^(NSUUID * _Nonnull uuid, NSError * _Nullable error) {

                successCallBack(@[]);
            }];
            break;
        }
    }
    
    return;

}

RCT_EXPORT_METHOD(setMode:(NSString *)uuid mode:(NSInteger) mode  callback:(RCTResponseSenderBlock) successCallBack) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        
        if ([button.uuid isEqualToString:uuid]) {
            
            if(FLICButtonTriggerModeClick == mode) {
               button.triggerMode = FLICButtonTriggerModeClick;
            } else if(FLICButtonTriggerModeClickAndHold == mode) {
               button.triggerMode = FLICButtonTriggerModeClickAndHold;
            } else if(FLICButtonTriggerModeClickAndDoubleClick == mode) {
               button.triggerMode = FLICButtonTriggerModeClickAndDoubleClick;
            } else {
               button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
            }

            successCallBack(@[]);
            break;
        }
    }
    
    return;

}

RCT_EXPORT_METHOD(setName:(NSString *)uuid name:(NSString *) name  callback:(RCTResponseSenderBlock) successCallBack) {
    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        if ([button.uuid isEqualToString:uuid]) {
            button.nickname = name;
            successCallBack(@[]);
            break;
        }
    }
    return;
}

- (void)stopScanForButton {

    [[FLICManager sharedManager] stopScan];
}

- (void)scanForButton;
{
        NSLog(@"A Flic2 scanForButton");
    [[FLICManager sharedManager] scanForButtonsWithStateChangeHandler:^(FLICButtonScannerStatusEvent event) {
        
        NSLog(@"A Flic2 scanForButtonsWithStateChangeHandler %ld", (long)event);
        // You can use these events to update your UI.
        switch (event)
        {
            case FLICButtonScannerStatusEventDiscovered:
                NSLog(@"A Flic2 was discovered.");
                [self sendEventScanStatusMessage: (@"discovered")];
                break;
            case FLICButtonScannerStatusEventConnected:
                NSLog(@"A Flic2 is being connected.");
                [self sendEventScanStatusMessage: (@"connected")];
                break;
            case FLICButtonScannerStatusEventVerified:
                NSLog(@"The Flic2 was verified successfully.");
                [self sendEventScanStatusMessage: (@"verified")];
                break;
            case FLICButtonScannerStatusEventVerificationFailed:
                NSLog(@"The Flic2 verification failed.");
                [self sendEventScanStatusMessage: (@"failed")];
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
            
            [self sendEventScanMessage: (button) event:(@"completion") errorCode:(0)];
        } else {
            [self sendEventScanMessage: (button) event:(@"completion") errorCode:(error.code)];
        }
    }];
}

- (void)managerDidRestoreState:(FLICManager *)manager;
{
    
    [self sendEventWithName:@"managerInitialized" body: @{}];
    
    // The mager was restored and can now be used.
    for (FLICButton *button in manager.buttons)
    {
        NSLog(@"Did restore Flic2: %@", button.name);
    }
}

- (void)manager:(nonnull FLICManager *)manager didUpdateState:(FLICManagerState)state {
    NSLog(@"Did update manager: %ld", (long)state);
}


// -- SCLFlicButtonDelegate --

- (void)buttonDidConnect:(FLICButton *)button;
{
    NSLog(@"Did connect Flic2: %@", button.name);
    [self sendEventMessage:(@"buttonConnectionCompleted") button:(button)];
}

- (void)button:(FLICButton *)button didDisconnectWithError:(NSError *)error;
{
    NSLog(@"Did disconnect Flic2: %@", button.name);
    if(!error) {
        [self sendEventMessage:(@"buttonDisconnected") button:(button)];
    } else {
        [self sendEventMessage:(@"buttonDisconnected") button:(button) errorCode:(error.code)];
    }
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
    NSLog(@"Did fail to connect Flic2: %@", button.name);
    if(!error) {
        [self sendEventMessage:(@"buttonConnectionFailure") button:(button)];

    } else {
        [self sendEventMessage:(@"buttonConnectionFailure") button:(button) errorCode:(error.code)];
    }
}

- (void)button:(FLICButton *)button didUnpairWithError:(NSError * _Nullable)error;
{
    NSLog(@"Did unpair with error Flic2: %@", button.name);
    if(!error) {
        [self sendEventMessage:(@"buttonConnectionUnpaired") button:(button)];

    } else {
        [self sendEventMessage:(@"buttonConnectionUnpaired") button:(button) errorCode:(error.code)];
    }

}

- (void)buttonIsReady:(nonnull FLICButton *)button {
     NSLog(@"Flic2: %@ is ready", button.name);
    [self sendEventMessage:(@"buttonConnectionReady") button:(button)];
}


@end
