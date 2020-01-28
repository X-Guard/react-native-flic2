# react-native-flic2
[![npm version](https://img.shields.io/npm/v/react-native-flic2)](https://www.npmjs.com/package/react-native-flic2) ![NPM](https://img.shields.io/npm/l/react-native-flic2) ![npm](https://img.shields.io/npm/dm/react-native-flic2) ![GitHub issues](https://img.shields.io/github/issues-raw/X-Guard/react-native-flic2)

This plugin enables you to connect to a Flic2 button made by Shortcut Labs.

This plugin is supported by the Flic2 SDKs
- Android: https://github.com/50ButtonsEach/flic2lib-android
- iOS: https://github.com/50ButtonsEach/flic2lib-ios

## Getting started

`$ npm install react-native-flic2 --save`

### Mostly automatic installation

`$ react-native link react-native-flic2`

## Usage
```javascript
import Flic2 from 'react-native-flic2';

// Flic2 Module
Flic2.isInitialized()                    // returns a boolean if the manager is initialized or not
Flic2.startScan();                       // start a scan
Flic2.stopScan();                        // stop a scan
Flic2.startService();                    // enable background capabilities through a service on Android, ignored by iOS
Flic2.addEventListener(event, fn);       // listen for button events (all buttons). Possible events are: didReceiveButtonDown, didReceiveButtonUp, didReceiveButtonClick, didReceiveButtonDoubleClick, didReceiveButtonHold
Flic2.connectAllKnownButtons();          // connect to known buttons
Flic2.buttonConnect(uuid);               // connect to a button with this uuid
Flic2.buttonForget(uuid);                // disconnect and forget the button
Flic2.forgetAllButtons();                // disconnect and forget all known buttons
Flic2.buttonDisconnect(uuid);            // disconnect a button with this uuid
Flic2.disconnectAllKnownButtons();       // disconnect all known buttons
Flic2.getButtons();                      // array of Flic2Button instances
Flic2.getButton(uuid);                   // get a button by uuid, returns a Flic2Button instance
Flic2.buttonSetMode(uuid, mode);         // change the button trigger mode by uuid. Use the constants to change the mode (see example below).
Flic2.buttonSetName(uuid, name);         // change the nickname by uuid

// Flic2Button instance definition
Flic2Button.addEventListener(event, fn); // listen for button events for this particular button. Possible events are: didReceiveButtonDown, didReceiveButtonUp, didReceiveButtonClick, didReceiveButtonDoubleClick, didReceiveButtonHold
Flic2Button.connect()                   // connect this button
Flic2Button.disconnect();                // disconnect this button
Flic2Button.forget();                    // removes the button completely
Flic2Button.getUuid();                   // get the button uuid
Flic2Button.getBluetoothAddress();       // get the button bluetooth address
Flic2Button.getName();                   // get the button nickname
Flic2Button.getBatteryLevel();           // get the battery state ("Ok", "Not ok")
Flic2Button.getVoltage();                // get the estimated battery voltage
Flic2Button.getPressCount();             // get button count since last reset
Flic2Button.getFirmwareRevision();       // get current hardware version
Flic2Button.getSerialNumber();           // get the serial number of the button
Flic2Button.getIsReady();                // get the ready state of the button
Flic2Button.getIsUnpaired();             // get the unpaired state of the button
Flic2Button.setMode(mode);               // change the button trigger for this particular button. Use the constants to change the mode (see example below).
Flic2Button.setName(name);               // sets the nickname of the button


// Constants
//
// These are the possible result codes for the variable 'result' in the
// event 'scanResult' (see example below)
//
// These constants are available through Flic2.constants
SCAN_RESULT_SUCCESS                                                     = 0;
SCAN_RESULT_ERROR_ALREADY_RUNNING                                       = 1;
SCAN_RESULT_ERROR_BLUETOOTH_NOT_ACTIVATED                               = 2;
SCAN_RESULT_ERROR_UNKNOWN                                               = 3;
SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED                           = 4;
SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE                   = 5;
SCAN_RESULT_ERROR_CONNECTION_TIMEOUT                                    = 6;
SCAN_RESULT_ERROR_INVALID_VERIFIER                                      = 7;
SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING  = 8;
SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED                      = 9;
SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON                     = 10;
SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH                            = 11;
SCAN_RESULT_ERROR_USER_CANCELED                                         = 12;
SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS                             = 13;
SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED                                  = 14;
SCAN_RESULT_ERROR_TOO_MANY_APPS                                         = 15;
SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY                        = 16;
SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES                 = 17;
SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION               = 18;
SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH                                   = 19;
SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED                              = 20;
SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE                                    = 21;

// These constants can be used to change the mode of the buttons
// These constants are available through Flic2.constants
BUTTON_TRIGGER_MODE_CLICK_AND_HOLD                                      = 0; 
BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK                              = 1; 
BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK_AND_HOLD                     = 2; 
BUTTON_TRIGGER_MODE_CLICK                                               = 3; 

// Scan result:
Flic2.addEventListener('scanResult', ((int) result, (Flic2Button) button) => {

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

# Example component
You can install and test this example at https://github.com/X-Guard/react-native-flic2-example

```javascript
// react and react native imports
import React, { Component } from 'react';
import { View, FlatList, TouchableOpacity, Text, StyleSheet, StatusBar, Platform, Vibration } from 'react-native';

// the Flic2 module
import Flic2 from 'react-native-flic2';

// icons
import { FontAwesomeIcon } from '@fortawesome/react-native-fontawesome';
import { faPause, faPlay, faTrash, faEdit, faBatteryEmpty, faBatteryQuarter, faBatteryHalf, faBatteryThreeQuarters, faBatteryFull } from '@fortawesome/free-solid-svg-icons';

// plugins to make it more fancy
import prompt from 'react-native-prompt-android';
import { request as requestPermission, PERMISSIONS } from 'react-native-permissions';
import * as Animatable from 'react-native-animatable';
import Toast from 'react-native-root-toast';

export default class App extends Component {

  constructor(props) {

    // man
    super(props);

    // init state
    this.state = {
      buttons: [],
      scanning: false,
    };

    // bindings
    this.didReceiveButtonClickFunction = this.didReceiveButtonClick.bind(this);
    this.onScanResultFunction = this.onScanResult.bind(this);

    // connect to all known buttons
    Flic2.connectAllKnownButtons();

    // start android service
    // don't worry, this function is ignored on iOS
    Flic2.startService();

    // get the buttons
    this.getButtons();

  }

  componentDidMount() {

    // listen bindings
    Flic2.addListener('didReceiveButtonClick', this.didReceiveButtonClickFunction);
    Flic2.addListener('scanResult', this.onScanResultFunction);

  }

  componentWillUnmount() {

    // remove bindings
    Flic2.removeListener('buttonEvent', this.handleButtonEventFunction);
    Flic2.removeListener('scanResult', this.onScanResultFunction);

  }

  async getButtons() {

    // async calls for init
    this.setState({
      buttons: await Flic2.getButtons(),
    });

  }

  async forgetAllButtons() {

    await Flic2.forgetAllButtons();
    this.getButtons();

  }

  async startScan() {

    // check os
    if (Platform.OS === 'android') {

      // on android we need the permission ACCESS_FINE_LOCATION first
      // we are just going to assume the permission is granted after calling this
      // in your real application, please create an actual permission check here
      await requestPermission(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);

    }

    // set to scanning
    this.setState({
      scanning: true,
    });

    // go!
    Flic2.startScan();

  }

  stopScan() {

    this.setState({
      scanning: false,
    });

    Flic2.stopScan();

  }

  connectButton(button) {

    // connect it
    button.connect(button);

    // update our button list
    this.getButtons();

  }

  disconnectButton(button) {

    // disconnect it
    button.disconnect(button);

    // update our button list
    this.getButtons();

  }

  forgetButton(button) {

    // forget it
    button.forget();

    // update our button list
    this.getButtons();

  }

  editButtonName(button) {

    // use the prompt to change the name
    prompt(
      'Edit Flic nickname',
      'Choose a name you will recognize',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'OK',
          onPress: value => {

            // save
            button.setName(value);

            // get new buttons
            this.getButtons();

          },
        },
      ],
      {
          type: 'plain-text',
          cancelable: true,
          defaultValue: button.getName(),
      }
    );

  }

  onScanResult(data) {

    this.setState({
      scanning: false,
    });

    // check
    if (data.error === false) {

      alert('The button has been added');
      this.getButtons();

    } else {

      if (data.result === Flic2.constants.SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE) {

        alert('This button is already connected to another device');

      } else
      if (data.result === Flic2.constants.SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED) {

        alert('No buttons found');

      } else {

        alert(`Could not connect\n\nError code: ${data.result}`);

      }


    }

  }

  didReceiveButtonClick(eventData) {

    console.log('Received click event', eventData);

    // update list
    this.getButtons();

    // do something with the click like showing a notification
    Toast.show(`Button ${eventData.button.getName()} has been pressed ${eventData.button.getPressCount()} times`);

    // wobble
    // we do this extensive check because when you develop the app with live reload, the _logoRef will break.
    if (typeof this._logoRef !== 'undefined' && this._logoRef !== null && typeof this._logoRef.wobble === 'function') {

      // wobble wobble
      this._logoRef.wobble();

    }

    // vibrate
    Vibration.vibrate(200);

  }

  getBatteryIcon(batteryPercentage) {

    if (batteryPercentage > 90) {

      return faBatteryFull;

    } else
    if (batteryPercentage > 75) {

      return faBatteryThreeQuarters;

    } else
    if (batteryPercentage > 40) {

      return faBatteryHalf;

    } else
    if (batteryPercentage > 15) {

      return faBatteryQuarter;

    } else {

      return faBatteryEmpty;

    }

  }


  render() {

    return (
      <View style={style.container}>
        <StatusBar barStyle="light-content" />

        {/* eslint-disable-next-line */}
        <Animatable.Image ref={ image => this._logoRef = image } style={style.logo} useNativeDriver={true} source={require('./images/flic-logo.png')} />

        {/* Scan button */}
        {this.state.scanning === false ?
          <TouchableOpacity onPress={this.startScan.bind(this)}>
            <View style={style.button}><Text style={style.buttonText}>Start scan</Text></View>
          </TouchableOpacity>
          :
          <TouchableOpacity onPress={this.stopScan.bind(this)}>
            <View style={style.button}><Text style={style.buttonText}>Scanning... (click to cancel)</Text></View>
          </TouchableOpacity> }

        <TouchableOpacity onPress={this.forgetAllButtons.bind(this)}>
          <View style={style.button}><Text style={style.buttonText}>Forget all buttons</Text></View>
        </TouchableOpacity>

        <View style={style.buttonContainer}>

          <Text style={style.heading}>Button list:</Text>

          {this.state.buttons.length > 0 ?
            <FlatList
              data={this.state.buttons}
              keyExtractor={item => item.uuid}
              renderItem={row => {

                // define button
                const button = row.item;

                // eslint-disable-next-line react-native/no-inline-styles
                return <View style={[style.listItem, { borderColor: button.getIsReady() ? '#006e1a' : '#b00000'}]}>
                  <FontAwesomeIcon style={style.icon} icon={this.getBatteryIcon(button.getBatteryLevel())} size={16} />
                  <Text style={style.pressCount}>{button.getPressCount()}</Text>
                  <Text style={style.listItemText}>{button.getName()}</Text>
                  <View style={style.icons}>
                    {button.getIsReady() === true ?
                      <TouchableOpacity onPress={this.disconnectButton.bind(this, button)}><FontAwesomeIcon icon={faPause} size={16} /></TouchableOpacity>
                      :
                      <TouchableOpacity onPress={this.connectButton.bind(this, button)}><FontAwesomeIcon icon={faPlay} size={16} /></TouchableOpacity>
                    }
                    <TouchableOpacity onPress={this.forgetButton.bind(this, button)}><FontAwesomeIcon icon={faTrash} size={16} /></TouchableOpacity>
                    <TouchableOpacity onPress={this.editButtonName.bind(this, button)}><FontAwesomeIcon icon={faEdit} size={16} /></TouchableOpacity>
                  </View>
                </View>;

              }}
            /> : <Text>There are no buttons paired to this app. Click 'start scan' and hold your flic button to add a new button.</Text>}

        </View>

      </View>
    );
  }
}


// define stylesheet
const style = StyleSheet.create({

  // container
  container: {
    paddingTop: 20,
    padding: 10,
    backgroundColor: '#45454d',
    flex: 1,
  },

  // logo
  logo: {
    width: 100,
    alignSelf: 'center',
    resizeMode: 'contain',
  },

  // button
  button: {
    padding: 10,
    borderRadius: 10,
    backgroundColor: '#ff0089',
    marginTop: 15,
  },
  buttonText: {
    color: 'white',
    textAlign: 'center',
    fontSize: 20,
  },

  // button container
  buttonContainer: {
    padding: 10,
    backgroundColor: 'rgba(255, 255, 255, 1)',
    borderRadius: 10,
    marginTop: 20,
  },

  // heading
  heading: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 10,
  },

  // list item
  listItem: {
    padding: 20,
    paddingLeft: 15,
    paddingRight: 15,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    borderRadius: 10,
    marginBottom: 10,
    backgroundColor: '#f3f9ff',
    borderWidth: 2,
  },
  listItemText: {
    flex: 1,
  },
  pressCount: {
    width: 25,
    color: 'rgba(40, 40, 40, 0.5)',
    fontSize: 10,
  },

  // icons
  icons: {
    width: 70,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    alignSelf: 'flex-end',
  },
  icon: {
    marginRight: 7,
  },

});
```

## Collaborating
We are happy to receive PRs! We have not published an NPM package before and are eager to learn!