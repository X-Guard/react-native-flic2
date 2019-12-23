#import "Flic2.h"
#import <flic2lib/flic2lib.h>

@implementation Flic2 {
    bool hasListeners;
}

NSString *eventNamespace = @"FLIC";

- (void) startObserving {
    hasListeners = YES;
}

- (void) stopObserving {
    hasListeners = NO;
}

- (void) sendEventMessage:(NSDictionary *)body {
    NSLog(@"FLICLIB EVENT %@", body);
    
    if (hasListeners) {
        [self sendEventWithName:eventNamespace body: body];
    }
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[eventNamespace];
}

// -- SCLFlicManagerDelegate --

RCT_EXPORT_METHOD(startup) {
    [FLICManager configureWithDelegate:self buttonDelegate:self background:YES];
    [self sendEventMessage: @{@"event": @"initFlicManager"}];
}

- (IBAction)startScan:(id)sender;
{
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
            button.triggerMode = FLICButtonTriggerModeClick;
        }
    }];
}

- (void)managerDidRestoreState:(FLICManager *)manager;
{
    // The mnager was restored and can now be used.
    for (FLICButton *button in manager.buttons)
    {
        NSLog(@"Did restore Flic: %@", button.name);
    }
}

// -- SCLFlicButtonDelegate --

- (void)buttonDidConnect:(FLICButton *)button;
{
    NSLog(@"Did connect Flic: %@", button.name);
}

- (void)button:(FLICButton *)button didDisconnectWithError:(NSError *)error;
{
    NSLog(@"Did disconnect Flic: %@", button.name);
}

- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age;
{
    NSLog(@"Flic: %@ was clicked", button.name);
}

- (void)button:(FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error;
{
    NSLog(@"Did fail to connect Flic:", button.name);
}

//RCT_EXPORT_METHOD(sampleMethod:(NSString *)stringArgument numberParameter:(nonnull NSNumber *)numberArgument callback:(RCTResponseSenderBlock)callback)
//{
//    // TODO: Implement some actually useful functionality
//    callback(@[[NSString stringWithFormat: @"numberArgument: %@ stringArgument: %@", numberArgument, stringArgument]]);
//}

@end
