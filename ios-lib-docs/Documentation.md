![Apple Logo](https://user-images.githubusercontent.com/2717016/70519799-d42e1900-1b3c-11ea-8966-4a6064fcb8e9.png)

# API Documentation

The *flic2lib.framework* for iOS contains 2 interfaces, *FLICManager* and *FLICButton*. Each interface has one delegate protocol that must be implemented, *FLICManagerDelegate* and *FLICButtonDelegate*, respectively.

For enum declarations, please scroll to the bottom.

## FLICButton

An instance of this class represents a physical Flic 2 button.

### Properties


```objc
@property(readonly, nonatomic, strong, nonnull) NSUUID *identifier;
```

&nbsp;&nbsp;&nbsp;&nbsp;This identifier is guaranteed to be the same for each Flic paired to a particular iOS device. Thus it can be used to identify a Flic within an app. However, If you need to identify Flics cross different apps on different iOS devices, then you should have look at the either uuid, serialNumber, or bluetoothAddress.

***

```objc
@property(weak, nonatomic, nullable) id<FLICButtonDelegate> delegate;
```

&nbsp;&nbsp;&nbsp;&nbsp;The delegate that will receive events related to this particular Flic. You can either set this delegate manually for each button, or let the manager do so automatically using the buttonDelegate as default.

***

```objc
@property(nonatomic, readonly, strong, nullable) NSString *name;
```

&nbsp;&nbsp;&nbsp;&nbsp;The bluetooth advertisement name of the Flic. This will be the same name that is shown by iOS it its bluetooth settings.

***

```objc
@property(nonatomic, readwrite, strong, nullable) NSString *nickname;
```

&nbsp;&nbsp;&nbsp;&nbsp;With this property you can read out the display name that the user may change in for example the Flic app. This value can also be changed from third party apps integrating this framework (including your app). The purpose of this is to provide more human readable name that the user can use to identify its Flic's across apps. For example "Kitchen Flic" or "Bedroom Lights". The nickname has a maximum length limit of 23 bytes. Keep in mind that this is the length in bytes, and not the number of UTF8 characters (which may be up to 4 bytes long). If you write anything longer than 23 bytes then the nickname will automatically be truncated to at most 23 bytes. When truncating the string, the framework will always cut between UTF8 character, so you don't have to worry about writing half an emoji, for example.

***

```objc
@property(nonatomic, readonly, strong, nonnull) NSString *bluetoothAddress;
```

&nbsp;&nbsp;&nbsp;&nbsp;The bluetooth address of the Flic. This will be a string representation of a 49 bit long address. Example: "00:80:e4:da:12:34:56"

***

```objc
@property(nonatomic, readonly, strong, nonnull) NSString *uuid;
```

&nbsp;&nbsp;&nbsp;&nbsp;This is a unique identifier string that best used to identify a Flic. This is for example used to identify Flics on all our API endpoints.

***

```objc
@property(nonatomic, readonly, strong, nonnull) NSString *serialNumber;
```

&nbsp;&nbsp;&nbsp;&nbsp;The serial number is a production identifier that is printed on the backside of the Flic inside the battery hatch. This serves no other purpose than allowing a user to identify a button by manually looking at it. Can be useful in some cases.

***

```objc
@property(nonatomic, readwrite) FLICButtonTriggerMode triggerMode;
```

&nbsp;&nbsp;&nbsp;&nbsp;Use this property to let the flic2lib know what type of click events you are interested it. By default you will get Click, Double Click and Hold events. However, if you for example are only interested in Click events then you can set this property to FLICButtonTriggerModeClick. Doing so will allow the flic2lib to deliver the events quicker since it can now ignore Double Click and Hold.

***

```objc
@property(nonatomic, readonly) FLICButtonState state;
```

&nbsp;&nbsp;&nbsp;&nbsp;Lets you know if the Flic is Connected, Disconnected, Connecting, or Disconnecting.

***

```objc
@property(nonatomic, readonly) uint32_t pressCount;
```

&nbsp;&nbsp;&nbsp;&nbsp;The number of times the Flic has been clicked since last time it booted.

***

```objc
@property(nonatomic, readonly) uint32_t firmwareRevision;
```

&nbsp;&nbsp;&nbsp;&nbsp;The revision of the firmware currently running on the Flic.

***

```objc
@property(nonatomic, readonly) BOOL isReady;
```

&nbsp;&nbsp;&nbsp;&nbsp;When a Flic connects it will go through a quick cryptographic verification to ensure that it is both a genuine Flic and that it is the correct Flic. Once this is completed this property will be set to YES and it is not until after that that you will start receiving click events (if any). As soon as the button disconnects this will be set to NO again.

***

```objc
@property(nonatomic, readonly) float batteryVoltage;
```

&nbsp;&nbsp;&nbsp;&nbsp;This will be the last know battery sample taken on the Flic. If this value is 0 then you should assume that no sample has yet been taken. It is important to know that the voltage may fluctuate depending on many different factors, such as temperature and workload. For example, heavy usage of the LED will temporarily lower the voltage, but it is likely to recover shortly after. Therefore we do not recomend to exactly translate this value into a battery percentage, instead consider showing a "change the battery soon"-status in your app once the voltage goes below 2.65V.

***

```objc
@property(nonatomic, readonly) BOOL isUnpaired;
```

&nbsp;&nbsp;&nbsp;&nbsp;If this property is YES, then it means that this app's pairing with this specific Flic is no longer valid. This can for example occur if the Flic has been factory reset, or if the maximum number of pairings have been reached. In this case you will need to delete the button from the manager and then scan for it again.

***

### Methods

```objc
- (void)connect;
```

&nbsp;&nbsp;&nbsp;&nbsp;Attempts to connect the Flic. If the Flic is not available, due to either being out of range or not advertising, then it will be connected once it becomes available as this call does not time out. This is often called a pending connection. It can be canceled by calling disconnect.

***

```objc
- (void)disconnect;
```

&nbsp;&nbsp;&nbsp;&nbsp;Disconnect a currently connected Flic or cancel a pending connection.

***

## FLICButtonDelegate

The delegate of a *FLICButton* instance must adopt the *FLICButtonDelegate* protocol.

### Required Methods


```objc
- (void)buttonDidConnect:(FLICButton *)button;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.

&nbsp;&nbsp;&nbsp;&nbsp;This method is called every time the Flic establishes a new bluetooth connection. Keep in mind that you also have to wait for the buttonIsReady: before the Flic is ready to be used.

***

```objc
- (void)buttonIsReady:(FLICButton *)button;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.

&nbsp;&nbsp;&nbsp;&nbsp;This method is called after each connection once the Flic has been cryptographically verified. You will not receive any click events before this is called.

***

```objc
- (void)button:(FLICButton *)button didDisconnectWithError:(NSError * _Nullable)error;
```

button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
error &nbsp;&nbsp;&nbsp;&nbsp;This error lets you know the reason for the disconnect. An error does not necessarily mean that something went wrong.

&nbsp;&nbsp;&nbsp;&nbsp;This method is called every time the bluetooth link with the Flic is lost. This can occur for several different reasons. The most common would be that the iOS device and the Flic is no longer within range of each other.

***

```objc
- (void)button:(FLICButton *)button didFailToConnectWithError:(NSError * _Nullable)error;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* error &nbsp;&nbsp;&nbsp;&nbsp;This error lets you know why the connection attempt failed.

&nbsp;&nbsp;&nbsp;&nbsp;This method is called when a connection attempt to a button fails. This indicates that something has gone wrong and that the pending connection will not be reset.

***

### Optional Methods


```objc
- (void)button:(FLICButton *)button didReceiveButtonDown:(BOOL)queued age:(NSInteger)age;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* queued &nbsp;&nbsp;&nbsp;&nbsp;Whether the event is a queued event that happened before the Flic connected or if it is a real time event.
* age &nbsp;&nbsp;&nbsp;&nbsp;If the event was queued, then this will let you know the age of the event rounded to the nearest second.

&nbsp;&nbsp;&nbsp;&nbsp;The Flic registered a button down event.

***


```objc
- (void)button:(FLICButton *)button didReceiveButtonUp:(BOOL)queued age:(NSInteger)age;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* queued &nbsp;&nbsp;&nbsp;&nbsp;Whether the event is a queued event that happened before the Flic connected or if it is a real time event.
* age &nbsp;&nbsp;&nbsp;&nbsp;If the event was queued, then this will let you know the age of the event rounded to the nearest second.

&nbsp;&nbsp;&nbsp;&nbsp;The Flic registered a button up event.

***


```objc
- (void)button:(FLICButton *)button didReceiveButtonClick:(BOOL)queued age:(NSInteger)age;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* queued &nbsp;&nbsp;&nbsp;&nbsp;Whether the event is a queued event that happened before the Flic connected or if it is a real time event.
* age &nbsp;&nbsp;&nbsp;&nbsp;If the event was queued, then this will let you know the age of the event rounded to the nearest second.

&nbsp;&nbsp;&nbsp;&nbsp;The Flic registered a click event.

***


```objc
- (void)button:(FLICButton *)button didReceiveButtonDoubleClick:(BOOL)queued age:(NSInteger)age;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* queued &nbsp;&nbsp;&nbsp;&nbsp;Whether the event is a queued event that happened before the Flic connected or if it is a real time event.
* age &nbsp;&nbsp;&nbsp;&nbsp;If the event was queued, then this will let you know the age of the event rounded to the nearest second.

&nbsp;&nbsp;&nbsp;&nbsp;The Flic registered a double click event.

***

```objc
- (void)button:(FLICButton *)button didReceiveButtonHold:(BOOL)queued age:(NSInteger)age;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* queued &nbsp;&nbsp;&nbsp;&nbsp;Whether the event is a queued event that happened before the Flic connected or if it is a real time event.
* age &nbsp;&nbsp;&nbsp;&nbsp;If the event was queued, then this will let you know the age of the event rounded to the nearest second.

&nbsp;&nbsp;&nbsp;&nbsp;The Flic registered a hold event.

***

```objc
- (void)button:(FLICButton *)button didUnpairWithError:(NSError * _Nullable)error;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The FLICButton instance that the event originated from.
* error &nbsp;&nbsp;&nbsp;&nbsp;This will always be nil at this time.

&nbsp;&nbsp;&nbsp;&nbsp;The app no longer has a valid pairing with the Flic button. The isUnpaired property will now be YES and all connection attempts made will immediately fail. To fix this you need to delete the button from the manager and then re-scan it again.

***

## FLICManager

This interface is intended to be used as a singleton. It keeps track of all the buttons that have been paired with this particular app and restores them on each application launch.

### Properties

```objc
@property(weak, nonatomic, nullable) id<FLICManagerDelegate> delegate;
```

&nbsp;&nbsp;&nbsp;&nbsp;The delegate that all the FLICManagerDelegate events will be sent to. Usually this is configured only once when the application is first launched, using the `configureWithDelegate:buttonDelegate:background:` method.

***


```objc
@property(weak, nonatomic, nullable) id<FLICButtonDelegate> buttonDelegate;
```

&nbsp;&nbsp;&nbsp;&nbsp;Once set, this delegate will automatically be set to every button instance on each application launch. This will also be assigned to new buttons that are discovered using the `scanForButtonsWithStateChangeHandler:completionHandler:` method. Using this delegate also ensures that the click events are delivered as fast as possible when an application is restored in the background.

***

```objc
@property(readonly) FLICManagerState state;
```

&nbsp;&nbsp;&nbsp;&nbsp;This is the state of the Flic manager. This state closely resembles the CBManager state of Apple's CoreBluetooth framework. You will only be able to communicate with Flic buttons while the manager is in the FlicManagerStatePoweredOn state.

***

### Methods

```
+ (instancetype _Nullable)sharedManager;
```

&nbsp;&nbsp;&nbsp;&nbsp;This class method return the singleton manager, assuming that it has been configured first using the `configureWithDelegate:buttonDelegate:background:` method first, otherwise nil is returned.

***
 
```
+ (instancetype _Nullable)configureWithDelegate:(id<FLICManagerDelegate> _Nullable)delegate
                                 buttonDelegate:(id<FLICButtonDelegate> _Nullable)buttonDelegate
                                     background:(BOOL)background;
```

* delegate &nbsp;&nbsp;&nbsp;&nbsp;The delegate to be used with the manager singleton.
* buttonDelegate &nbsp;&nbsp;&nbsp;&nbsp;The delegate to be automatically assigned to each FLICButton instance.
* background &nbsp;&nbsp;&nbsp;&nbsp;Whether or not you intend to use this application in the background.

&nbsp;&nbsp;&nbsp;&nbsp;This configuration method must be called before the manager can be used. It is recommended that this is done as soon as possible after your application has launched in order to minimize the delay of any pending click events. The flic2lib officially only support iOS 12 and up, however, to make it easier for the developer, the framework is built with a target of iOS 9 and contains slices for both arm64 and armv7. This means that you will be able to include, and load, the framework in apps that support iOS 9. However, if you try to configure the manager on a device running iOS 11, or below, then the manager state will switch to FLICManagerStateUnsupported. A good place to handle this would be in the manager:didUpdateSate: method.


***

```objc
- (NSArray<FLICButton *> *)buttons;
```

&nbsp;&nbsp;&nbsp;&nbsp;This array will contain every button that is currently paired with this application. Each button will be added to the list after a call to `scanForButtonsWithStateChangeHandler:completionHandler:` has completed without error. It is important to know that you may not try to access this list until after the managerDidRestoreState: method has been sent to the manager delegate.

***

```objc
- (void)forgetButton:(FLICButton *)button completion:(void (^)(NSUUID *uuid, NSError * _Nullable error))completion;
```

* button &nbsp;&nbsp;&nbsp;&nbsp;The button that you wish to delete from the manager.
* completion &nbsp;&nbsp;&nbsp;&nbsp;This returns the identifier of the button instance that was just removed along with an error, if needed.

&nbsp;&nbsp;&nbsp;&nbsp;This will delete this button from the manager. After a successful call to this method you will no longer be able to communicate with the associated Flic button unless you pair it again using the scanForButtonsWithStateChangeHandler:completionHandler:. On completion, the button will no longer be included in the manager's buttons array. After a successful call to this method, you should discard of any references to that particular Flic button object. If you try to forget a button that is already forgotten, then you will get an error with the FLICErrorAlreadyForgotten code.

***

```objc
- (void)scanForButtonsWithStateChangeHandler:(void (^)(FLICButtonScannerStatusEvent event))stateHandler
                                  completion:(void (^)(FLICButton * _Nullable button, NSError * _Nullable error))completion;
```

* stateHandler &nbsp;&nbsp;&nbsp;&nbsp;This handler returns status events that lets you know what the scanner is currently doing. The purpose of this handler is to provide a predefined states where you may update your application UI.
* completion &nbsp;&nbsp;&nbsp;&nbsp;The completion handler will always run and if successful it will return a new FLICButton instance, otherwise it will provide you with an error.

&nbsp;&nbsp;&nbsp;&nbsp;This method lets you add new Flic buttons to the manager. The scan flow is automated and the stateHandler will let you know what information you should provide to your application end user. If you try to scan for buttons while running on an iOS version below the minimum requirement, then you will get an error with the FLICErrorUnsupportedOSVersion error code.

***

```objc
- (void)stopScan;
```

&nbsp;&nbsp;&nbsp;&nbsp;Cancel an ongoing button scan. This will result in a scan completion with an error.

***

## FLICManagerDelegate

The delegate of a *FLICManager* instance must adopt the *FLICManagerDelegate* protocol.

### Required Methods

```objc
- (void)managerDidRestoreState:(FLICManager *)manager;
```

* manager &nbsp;&nbsp;&nbsp;&nbsp;The manager instance that the event originates from. Since this is intended to be used as a singleton, there should only ever be one manager instance.

&nbsp;&nbsp;&nbsp;&nbsp;This is called once the manager has been restored. This means that all the FLICButton instances from your previous session are restored as well. After this method has been called you may start using the manager and communicate with the Flic buttons. This method will only be called once on each application launch.


***

```objc
- (void)manager:(FLICManager *)manager didUpdateState:(FlicManagerState)state;
```

* manager &nbsp;&nbsp;&nbsp;&nbsp;The manager instance that the event originates from. Since this is intended to be used as a singleton, there should only ever be one manager instance.
* state &nbsp;&nbsp;&nbsp;&nbsp;The state of the Flic manager singleton.

&nbsp;&nbsp;&nbsp;&nbsp;Each time the state of the Flic manager changes, you will get this callback. Usually this is related to bluetooth state changes on the iOS device. It is also guaranteed to be called at least once after the manager has been configured.

***

## Constants

* FLICErrorDomain

	``com.shortcutlabs.flic2lib``

* FLICButtonScannerErrorDomain

	``com.shortcutlabs.flic2lib.buttonscanner``

## Enums

### FLICManagerState

Represents the the different states that a Flic manager can be in at any given time. These states are mostly translated values of Apple's CoreBluetooth CBManagerState enums.

* FLICManagerStateUnknown

	``State is unknown, update imminent.``

* FLICManagerStateResetting

	``The bluetooth connection with the system service was momentarily lost, update imminent.``

* FLICManagerStateUnsupported

	``The Flic manager can not be used on this platform.``

* FLICManagerStateUnauthorized

	``The application is not authorized to use Bluetooth Low Energy.``

* FLICManagerStatePoweredOff

	``Bluetooth is currently powered off.``

* FLICManagerStatePoweredOn

	``Bluetooth is currently powered on and available to use.``

***

### FLICButtonScannerErrorCode

Represents the different error codes that can be generated while scanning and pairing new Flics.

* FLICButtonScannerErrorCodeUnknown

	``The scan was unsuccessful due to an unknown reason.``
 
* FLICButtonScannerErrorCodeBluetoothNotActivated

	``The scan could not be started since bluetooth was not in the powered on state.``
 
* FLICButtonScannerErrorCodeNoPublicButtonDiscovered

	``No button was advertising in public mode within proximity.``
 
* FLICButtonScannerErrorCodeBLEPairingFailedPreviousPairingAlreadyExisting

	``The bluetooth pairing failed since the user already has paired this button before with this device. This is solved by removing the pairing from the iOS bluetooth pairing settings screen.``
 
* FLICButtonScannerErrorCodeBLEPairingFailedUserCanceled

	``The bluetooth pairing failed since the user pressed cancel on the pairing dialog.``
 
* FLICButtonScannerErrorCodeBLEPairingFailedUnknownReason

	``The bluetooth pairing failed since iOS decided to decline the request. It is unknown why or if this can happen.``
 
* FLICButtonScannerErrorCodeAppCredentialsDontMatch

	``Indicates that the button cannot be unlocked since it belongs to a different brand.``

* FLICButtonScannerErrorCodeUserCanceled

	``The scan was manually canceled using the stopScan method.``

* FLICButtonScannerErrorCodeInvalidBluetoothAddress

	``The Flic's certificate belongs to a different bluetooth address.``

* FLICButtonScannerErrorCodeGenuineCheckFailed

	``The framework was unable to pair with the Flic since it did not pass the authenticity check.``

* FLICButtonScannerErrorCodeAlreadyConnectedToAnotherDevice

	``The discovered Flic cannot be connected since it is currently connected to a different device.``

* FLICButtonScannerErrorCodeTooManyApps

	``The discovered Flic cannot be connected since the maximum number of simultaneous app connections has been reached.``

* FLICButtonScannerErrorCodeCouldNotSetBluetoothNotify

	``A bluetooth specific error. The framework was unable to establish a connection to the Flic peripheral.``

* FLICButtonScannerErrorCodeCouldNotDiscoverBluetoothServices

	``A bluetooth specific error. The framework was unable to establish a connection to the Flic peripheral.``

* FLICButtonScannerErrorCodeButtonDisconnectedDuringVerification

	``The bluetooth connection was dropped during the verification process.``

* FLICButtonScannerErrorCodeConnectionTimeout

	``The Flic peripheral did not connect in time.``

* FLICButtonScannerErrorCodeFailedToEstablish

	``The bluetooth connection failed.``

* FLICButtonScannerErrorCodeConnectionLimitReached

	``The iOS device reached the maximum number of allowed bluetooth peripherals.``

* FLICButtonScannerErrorCodeInvalidVerifier

	``The signature generated by the Flic button could not be verified.``

* FLICButtonScannerErrorCodeNotInPublicMode

	``The Flic button was no longer in public mode when the verification process ran.``

***

### FLICButtonScannerStatusEvent

While the scanner is running, it will send a status events to let you know what it is doing. These enums represents those events.

* FLICButtonScannerStatusEventDiscovered
	
	``A public Flic has been discovered and a connection attempt will now be made.``

* FLICButtonScannerStatusEventConnected
	
	``The Flic was successfully bluetooth connected.``

* FLICButtonScannerStatusEventVerified
	
	``The Flic has been verified and unlocked for this app. The Flic will soon be delivered in the assigned completion handler.``
	
* FLICButtonScannerStatusEventVerificationFailed
	
	``The Flic could not be verified. The completion handler will soon run to let you know what the error was.``

***

### FLICError

These enums represents the different error codes that can be sent from flic2lib, excluding the button scanner which has its own set of error codes.

* FLICErrorUnknown

	``An unknown error has occurred.``

* FLICErrorNotConfigured

	``You are trying to use the manager while it has not been configured yet.``

* FLICErrorCouldNotDiscoverBluetoothServices

	``A bluetooth specific error code. This means that something went wrong while the phone tried to establish a connection with the Flic peripheral.``

* FLICErrorVerificationSignatureMismatch

	``The framework was unable to verify the cryptographic signature from the Flic while setting up a session.``

* FLICErrorInvalidUuid

	``The UUID of a button is not correct.``

* FLICErrorGenuineCheckFailed

	``While establishing a connection with the Flic the framework was unable to verify the authenticity of the button.``

* FLICErrorTooManyApps

	``The app was unable to establish a connection with the Flic button because it already had a connection with too many apps on this particular phone.``

* FLICErrorUnpaired

	``The pairing on the Flic button has been lost so the app's pairing data is no longer valid. This typically happens if the Flic is factory reset.``

* FLICErrorUnsupportedOSVersion

	``The manager was unable to complete the task since the device is not running on a supported iOS version.``

* FLICErrorAlreadyForgotten

	``You are trying to use a FLICButton object that has already been forgotten by the manager. Please discard of your references to this object.``


***

### FLICButtonState

The different states that a Flic can be in at any given time.

* FLICButtonStateDisconnected

	``The Flic is currently disconnected and a pending connection is not set. The Flic will not connect again unless you manually call the connect method.``

* FLICButtonStateConnecting

	``The Flic is currently disconnected, but a pending connection is set. The Flic will automatically connect again as soon as it becomes available.``

* FLICButtonStateConnected

	``The Flic currently has a bluetooth connection with the phone. This does not necessarily mean that it has been verified. Please listen for the isReady event, or read the isReady property, for that information.``

* FLICButtonStateDisconnecting

	``The Flic is currently connected, but is attempting to disconnect. Typically this state will only occur for very short periods of time before either switching to the connecting or disconnected state again.``


### FLICButtonTriggerMode

The different trigger modes that you can configure the Flic button to use.

* FLICButtonTriggerModeClickAndHold
	
	```
	Used to distinguish between only click and hold.
	
	Click will be fired when the button is released if it was pressed for maximum 1 second. Otherwise, hold will be fired 1 second after the button was pressed. Click will then not be fired upon release.
	Since this option will only distinguish between click and hold it does not have to take double click into consideration.
	This means that the click event can be sent immediately on button release rather than to wait for a possible double click.
	
	Note: this will be the default behavior.
	```

* FLICButtonTriggerModeClickAndDoubleClick
	
	```
	Used to distinguish between only single click and double click.
	
	Double click will be registered if the time between two button down events was at most 0.5 seconds.
	The double click event will then be fired upon button release.
	
	If the time was more than 0.5 seconds, a single click event will be fired; either directly upon button release if the button was down for more than 0.5 seconds, or after 0.5 seconds if the button was down for less than 0.5 seconds.
	```

* FLICButtonTriggerModeClickAndDoubleClickAndHold
	
	```
	Used to distinguish between single click, double click and hold.
	
	If the time between the first button down and button up event was more than 1 second, a hold event will be fired.
	Else, double click will be fired if the time between two button down events was at most 0.5 seconds.
	The double click event will then be fired upon button release.
	
	If the time was more than 0.5 seconds, a single click event will be fired; either directly upon button release if the button was down for more than 0.5 seconds, or after 0.5 seconds if the button was down for less than 0.5 seconds.
	
	Note: Three fast consecutive clicks means one double click and then one single click. Four fast consecutive clicks means two double clicks.
	```

* FLICButtonTriggerModeClick
	
	```
	This mode will only send click and the event will be sent directly on buttonDown.
	This will be the same as listening for buttonDown.
	
	Note: This is optimal if your application requires the lowest latency possible.
	```

***


