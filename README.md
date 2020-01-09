# react-native-flic2
[![npm version](https://img.shields.io/npm/v/react-native-flic2)](https://www.npmjs.com/package/react-native-flic2) ![NPM](https://img.shields.io/npm/l/react-native-flic2) ![npm](https://img.shields.io/npm/dm/react-native-flic2) ![GitHub issues](https://img.shields.io/github/issues-raw/X-Guard/react-native-flic2)

This plugin enables you to connect to a Flic2 button made by Shortcut Labs.

This plugin is supported by the Flic2 SDKs
- Android: https://github.com/50ButtonsEach/flic2lib-android
- iOS: https://github.com/50ButtonsEach/flic2lib-ios

## Timeline
In december we added the foundation for this plugin. We are now working to create a consistent API across iOS and Android with support for the most popular use cases. 

Current implementation state: INSTABLE

Most functions do not work yet. The first testable version of this module should be released before February.

## Getting started

`$ npm install react-native-flic2 --save`

### Mostly automatic installation

`$ react-native link react-native-flic2`

## Usage
```javascript
import Flic2 from 'react-native-flic2';

// Flic2 Module
Flic2.startScan(); // start a scan
Flic2.stopScan(); // stop a scan
Flic2.connectAllKnownButtons(); // connect to known buttons
Flic2.startService(); // enable background capabilities through a service on Android, ignored by iOS
Flic2.getButtons(); // array of Flic2Button instances
Flic2.getButton(uuid); // get a button by uuid, returns a Flic2Button instance
Flic2.addEventListener(event, fn); // listen for button events (all buttons). Possible events are: didReceiveButtonDown, didReceiveButtonUp, didReceiveButtonClick, didReceiveButtonDoubleClick, didReceiveButtonHold

// Flic2Button instance definition
Flic2Button.connecct() // connect this button
Flic2Button.disconnect(); // disconnect this button
Flic2Button.forget(); // removes the button completely
Flic2Button.getUuid(); // get the button uuid
Flic2Button.getBluetoothAddress(); // get the button bluetooth address
Flic2Button.getName(); // get the internal button name
Flic2Button.getBatteryLevel(); // get the estimated battery level
Flic2Button.getVoltage(); // get the estimated battery voltage
Flic2Button.getPressCount(); // get button count since last reset
Flic2Button.getFirmwareRevision(); // get current hardware version
Flic2Button.addEventListener(event, fn); // listen for button events for this particular button. Possible events are: didReceiveButtonDown, didReceiveButtonUp, didReceiveButtonClick, didReceiveButtonDoubleClick, didReceiveButtonHold

// constants
const SCAN_RESULT_SUCCESS                                                     = 0;
const SCAN_RESULT_ERROR_ALREADY_RUNNING                                       = 1;
const SCAN_RESULT_ERROR_BLUETOOTH_NOT_ACTIVATED                               = 2;
const SCAN_RESULT_ERROR_UNKNOWN                                               = 3;
const SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED                           = 4;
const SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE                   = 5;
const SCAN_RESULT_ERROR_CONNECTION_TIMEOUT                                    = 6;
const SCAN_RESULT_ERROR_INVALID_VERIFIER                                      = 7;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING  = 8;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED                      = 9;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON                     = 10;
const SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH                            = 11;
const SCAN_RESULT_ERROR_USER_CANCELED                                         = 12;
const SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS                             = 13;
const SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED                                  = 14;
const SCAN_RESULT_ERROR_TOO_MANY_APPS                                         = 15;
const SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY                        = 16;
const SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES                 = 17;
const SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION               = 18;
const SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH                                   = 19;
const SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED                              = 20;
const SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE                                    = 21;

// Scan result:
Flic2.addEventListener('sanResult', ((int) result, (Flic2Button) button) => {

	if (result === Flic2.constants.SCAN_RESULT_SUCCESS) {

		doSomethingWithButton(button);

	} else 
	if(result === Flic2.constants.SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE) {

		alert('This button is already connected to another device');

	}
	// ... etc

});

// Button events:
// 
// didReceiveButtonDown
// didReceiveButtonUp
// didReceiveButtonClick
// didReceiveButtonDoubleClick
// didReceiveButtonHold
Flic2.addEventListener('didReceiveButtonHold', ((object) eventData) => {

	// eventData:
	// { int age, bool queued, Flic2Button button }

});
```

## Collaborating
We are happy to receive PRs! We have not published an NPM package before and are eager to learn!