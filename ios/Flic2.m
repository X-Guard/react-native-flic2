#import "Flic2.h"
#import <flic2lib/flic2lib.h>

@implementation Flic2 {
    bool hasListeners;
}

+ (BOOL) requiresMainQueueSetup {
    return YES;
}

NSString *testNameSpace = @"FLIC2";

- (void) startObserving {
    hasListeners = YES;
}

- (void) stopObserving {
    hasListeners = NO;
}

- (void) sendEventMessage:(NSDictionary *)body {
    NSLog(@"FLIC2LIB EVENT %@", body);
    
    if (hasListeners) {
        [self sendEventWithName:testNameSpace body: body];
    }
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[testNameSpace];
}

-(NSDictionary *) convertButtonToDict: (FLICButton *) button
{
        NSLog(@"FLIC2LIB convertButtonToDict %@", button.name);
    return @{
             @"uuid": button.uuid,
             @"bluetoothAddress": button.bluetoothAddress,
             @"name": button.name,
             @"batteryLevel": @(button.batterylevel),
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

    for (FLICButton *button in buttons) {
      [buttonArray addObject: [self convertButtonToDict: button]];
    }

    callback(@[buttonArray]);
}

RCT_EXPORT_METHOD(connectAllKnownButtons) {
    [self sendEventMessage: @{@"event": @"connectKnownButtons"}];

    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        NSLog(@"Connect button: %@", button);
        button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
        [button connect];
    }
}

RCT_EXPORT_METHOD(startScan) {
    
    [self scanForButton];
}

RCT_EXPORT_METHOD(stopScanning) {
    [self stopScan];
}

RCT_EXPORT_METHOD(forgetAllButtons:(NSString *) uuid) {

    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {

        [[FLICManager sharedManager] forgetButton:(button) completion:^(NSUUID * _Nonnull uuid, NSError * _Nullable error) {
                [self sendEventMessage: @{@"event": @"forgotButton"}];
        }];
    }
}

RCT_EXPORT_METHOD(forgetButton:(NSString *) uuid) {

    NSArray<FLICButton *> *buttons = [FLICManager sharedManager].buttons;

    for (FLICButton *button in buttons) {
        if (button.uuid == uuid) {
            [[FLICManager sharedManager] forgetButton:(button) completion:^(NSUUID * _Nonnull uuid, NSError * _Nullable error) {
                    [self sendEventMessage: @{@"event": @"forgotButton"}];
            }];
            break;
        }
    }
}

- (IBAction)stopScan {

    [[FLICManager sharedManager] stopScan];
    [self sendEventMessage: @{@"event": @"stopScan"}];
}

- (IBAction)scanForButton;
{
    [self sendEventMessage: @{@"event": @"scanForButton"}];
    [[FLICManager sharedManager] scanForButtonsWithStateChangeHandler:^(FLICButtonScannerStatusEvent event) {
        // You can use these events to update your UI.
        switch (event)
        {
            case FLICButtonScannerStatusEventDiscovered:
                NSLog(@"A Flic was discovered.");
                break;
            case FLICButtonScannerStatusEventConnected:
                NSLog(@"A Flic is being verified.");
                break;
            case FLICButtonScannerStatusEventVerified:
                NSLog(@"The Flic was verified successfully.");
                break;
            case FLICButtonScannerStatusEventVerificationFailed:
                NSLog(@"The Flic verification failed.");
                break;
            default:
                break;
        }
    } completion:^(FLICButton *button, NSError *error) {
        NSLog(@"Scanner completed with error: %@", error);
        if (!error)
        {
            NSLog(@"Successfully verified: %@, %@, %@", button.name, button.bluetoothAddress, button.serialNumber);
            // Listen to single click only.
            button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
            [button connect];
        }
    }];
}

- (void)managerDidRestoreState:(FLICManager *)manager;
{
    // The mnager was restored and can now be used.
    for (FLICButton *button in manager.buttons)
    {
        NSLog(@"Did restore Flic: %@", button.name);
        button.triggerMode = FLICButtonTriggerModeClickAndDoubleClickAndHold;
        [button connect];
    }
}

- (void)manager:(nonnull FLICManager *)manager didUpdateBluetoothState:(CBManagerState)state {
    NSLog(@"Update bleutooth state: %ld", (long)state);
    [self sendEventMessage: @{@"event": @"didUpdateBluetoothState", @"state": @((long)state)}];
}


// -- SCLFlicButtonDelegate --

- (void)buttonDidConnect:(FLICButton *)button;
{
    NSLog(@"Did connect Flic: %@", button.name);
    [self sendEventMessage: @{
        @"event": @"flicButtonDidConnect",
        @"button": @[[self convertButtonToDict: button]],
    }];
}

- (void)button:(FLICButton *)button didDisconnectWithError:(NSError *)error;
{
    NSLog(@"Did disconnect Flic: %@", button.name);
    [self sendEventMessage: @{
        @"event": @"didDisconnectWithError",
        @"button": @[[self convertButtonToDict: button]],
        @"error": error
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonDown:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic: %@ was clicked down", button.name);
    [self sendEventMessage: @{
        @"event": @"didReceiveButtonDown",
        @"button": @[[self convertButtonToDict: button]],
        @"queued": @(queued),
        @"age": @(age)
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonUp:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic: %@ was clicked up", button.name);
    [self sendEventMessage: @{
        @"event": @"didReceiveButtonUp",
        @"button": @[[self convertButtonToDict: button]],
        @"queued": @(queued),
        @"age": @(age)
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic: %@ was clicked", button.name);
    [self sendEventMessage: @{
        @"event": @"didReceiveButtonClick",
        @"button": @[[self convertButtonToDict: button]],
        @"queued": @(queued),
        @"age": @(age)
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonDoubleClick:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic: %@ was clicked double", button.name);
    [self sendEventMessage: @{
        @"event": @"didReceiveButtonDoubleClick",
        @"button": @[[self convertButtonToDict: button]],
        @"queued": @(queued),
        @"age": @(age)
    }];
}

- (void)button:(FLICButton *)button didReceiveButtonHold:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic: %@ was hold", button.name);
    [self sendEventMessage: @{
        @"event": @"didReceiveButtonHold",
        @"button": @[[self convertButtonToDict: button]],
        @"queued": @(queued),
        @"age": @(age)
    }];
}

- (void)button:(FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error;
{
    NSLog(@"Did fail to connect Flic: %@", button.name);
    [self sendEventMessage: @{
        @"event": @"didFailToConnectWithError",
        @"button": @[[self convertButtonToDict: button]],
        @"error": error
    }];
}

- (void)button:(FLICButton *)button didUnpairWithError:(NSError * _Nullable)error;
{
    NSLog(@"Did unpair with error Flic: %@", button.name);
    [self sendEventMessage: @{
        @"event": @"didUnpairWithError",
        @"button": @[[self convertButtonToDict: button]],
        @"error": error
    }];
}

- (void)buttonIsReady:(nonnull FLICButton *)button {
     NSLog(@"Flic: %@ is ready", button.name);
    [self sendEventMessage: @{
        @"event": @"isReady",
        @"button": @[[self convertButtonToDict: button]],
    }];
}


@end
