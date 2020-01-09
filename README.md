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
```

## Collaborating
We are happy to receive PRs! We have not published an NPM package before and are eager to learn!