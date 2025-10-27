![Apple Logo](https://user-images.githubusercontent.com/2717016/70519799-d42e1900-1b3c-11ea-8966-4a6064fcb8e9.png)

# iOS Tutorial

You are just a few steps away from having Flic 2 functionality in your iOS app. This tutorial will show you how to implement the basic features of flic2lib starting from a blank Xcode project. The framework is distributed as an XCFramework bundle, which means that it will, by default, run on both device and simulator targets. 

## Set up Xcode

1. Place the downloaded `flic2lib.xcframework` folder somewhere on you computer, preferably in your project's folder structure.

2. Drag-and-drop the whole `flic2lib.xcframework` folder from your finder window to the `Frameworks, Libraries, and Embedded Content` section under
the `General` tab of your application's target setting. 
	
	Ensure that `flic2lib.xcframework` is listed as *Embed & Sign*:
	
	<img width="480" alt="Frameworks, Libraries and Embedded Content" src="https://user-images.githubusercontent.com/2717016/78569793-682a9b80-7824-11ea-98f9-ca0ee03ae0f3.png">
	
	Doing this should automatically add the xcframework to `Link Binary With Libraries` under the Build Phases tab.

2. Under `Project Settings` -&gt; `Build Settings`, set the flag `Allow Non-modular includes in Framework Modules` to **Yes**:
	
	![Allow non Modular](https://user-images.githubusercontent.com/2717016/70518151-26216f80-1b3a-11ea-8b05-4f61a58e5ce1.png)
3. In your project settings, check the `Uses Bluetooth LE accessories` option under `Background Modes` in the `Signing & Capabilities` tab.
	
	*Note:* If you can't see the options for `Background Modes` then you need to add that capability by pressing **+** in the top left corner of that view.
	
	Checking this option will let iOS know that we want permission to communicate with Flic buttons while the application is in the background.
	
	![Background Modes](https://user-images.githubusercontent.com/2717016/70518152-26ba0600-1b3a-11ea-9379-00d4638d3f35.png)

4. It the project's Info.plist file we must now add two keys:
	
	* Privacy - Bluetooth Peripheral Usage Description
	* Privacy - Bluetooth Always Usage Description
	
	The first one specifies that we wish to connect with Bluetooth Peripherals and the second one specifies that we will do so in the background. The values that you add here will be displayed to the user when iOS shows its bluetooth permission dialogs.
	
	![Plist Values](https://user-images.githubusercontent.com/2717016/70518156-27529c80-1b3a-11ea-86b1-65f75c705d66.png)

## Objective-C Code

1. Import the xcframework module by addinng `@ import flic2lib;` wherever it suits your application the best. If you are using Swift, then type `@import flic2lib` instead. In this case we will just add it to the view controller implementation file. We also went ahead and added both of the Button and Manager delegate protocols.
	
	```objc
	@ import flic2lib;
	
	@interface ViewController () <FLICButtonDelegate, FLICManagerDelegate>
	
	@end
	```

2. The first thing we need to do before we can start using the Flic Manager is to configure its singleton. This needs to be done on every application launch.
	
	In this case we will do so in the `viewDidLoad` method. The background execution flag specifies whether or not you intend to use the buttons in the background, so we will set it to **YES**.
	
	```obj-c
	- (void)viewDidLoad
	{
		[super viewDidLoad];
		[FLICManager configureWithDelegate:self buttonDelegate:self background:YES];
	}
	```

3. Now that the manager is configured we can go ahead and discover our first Flic by starting a scan. You will most likely only want the scan to start on some sort of user interaction. In this example we have created a UI button that is tied to the IBAction `startScan`.
	
	The manager has an integrated scan wizard that takes care of Flic discovery and pairing. When starting a scan we need to create two handlers, one event handler, and one completion handler. The event handler is meant as a way to provide information on what is going on and it is a good idea to update the app UI on those events. The completion handler will always run regardless if a button was found or not.
	
	```objc
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
	```

4. At this point we are mostly done, but let's add some more delegate methods so that we can actually make use of the Flics.
	
	It is good practice to newer call any methods on the manager before `managerDidRestoreState:` has been called.
	
	```objc
	- (void)managerDidRestoreState:(FLICManager *)manager;
	{
		// The manager was restored and can now be used.
		for (FLICButton *button in manager.buttons)
		{
			NSLog(@"Did restore Flic: %@", button.name);
		}
	}
	```

5. The `manager:didUpdateState:` is recommended in order to keep track of the state changes. Here you should handle the `FLICManagerStateUnsupported` state in case you intend to include the framework on devices with a deployment target lower than iOS 12. Please read the [OS Compatibility](https://github.com/50ButtonsEach/flic2lib-ios/wiki/OS-Compatibility) document if that is relevant to you.

	```objc
	- (void)manager:(FLICManager *)manager didUpdateState:(FLICManagerState)state;
	{
		switch (state)
		{
			case FLICManagerStatePoweredOn:
				// Flic buttons can now be scanned and connected.
				NSLog(@"Bluetooth is turned on");
				break;
			case FLICManagerStatePoweredOff:
				// Bluetooth is not powered on.
				NSLog(@"Bluetooth is turned off");
				break;
			case FLICManagerStateUnsupported:
				// The framework can not run on this device.
				NSLog(@"FLICManagerStateUnsupported");
			default:
				break;
		}
	}
	```

6. Let's add some log printing as well to verify that everything is working:
	
	```objc
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
	```

That's it!

***