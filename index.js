// get the native module
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

// event emitter
import EventEmitter from 'react-native/Libraries/vendor/emitter/EventEmitter';

// local imports
import Flic2Button from './flic2Button.js';

// we only care about flic 2 here
const Flic2Module = NativeModules.Flic2;

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

const BUTTON_TRIGGER_MODE_CLICK_AND_HOLD                                      = 0;
const BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK                              = 1;
const BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK_AND_HOLD                     = 2;
const BUTTON_TRIGGER_MODE_CLICK                                               = 3;

/**
 * React Native Flic 2
 * Exposes the Flic2Module functions to the React Native context.
 *
 * @version 1.0.0
 */
class Flic2 extends EventEmitter {

  /**
   * Constructor.
   *
   * @class
   * @version 1.0.0
   */
  constructor() {

    // extended class initialisation
    super();

    this.isInitialized = false;

    // define constants
    this.constants = {
      SCAN_RESULT_SUCCESS: SCAN_RESULT_SUCCESS,
      SCAN_RESULT_ERROR_ALREADY_RUNNING: SCAN_RESULT_ERROR_ALREADY_RUNNING,
      SCAN_RESULT_ERROR_BLUETOOTH_NOT_ACTIVATED: SCAN_RESULT_ERROR_BLUETOOTH_NOT_ACTIVATED,
      SCAN_RESULT_ERROR_UNKNOWN: SCAN_RESULT_ERROR_UNKNOWN,
      SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED: SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED,
      SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE: SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE,
      SCAN_RESULT_ERROR_CONNECTION_TIMEOUT: SCAN_RESULT_ERROR_CONNECTION_TIMEOUT,
      SCAN_RESULT_ERROR_INVALID_VERIFIER: SCAN_RESULT_ERROR_INVALID_VERIFIER,
      SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING: SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING,
      SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED: SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED,
      SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON: SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON,
      SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH: SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH,
      SCAN_RESULT_ERROR_USER_CANCELED: SCAN_RESULT_ERROR_USER_CANCELED,
      SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS: SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS,
      SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED: SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED,
      SCAN_RESULT_ERROR_TOO_MANY_APPS: SCAN_RESULT_ERROR_TOO_MANY_APPS,
      SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY: SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY,
      SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES: SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES,
      SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION: SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION,
      SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH: SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH,
      SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED: SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED,
      SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE: SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE,
      BUTTON_TRIGGER_MODE_CLICK_AND_HOLD: BUTTON_TRIGGER_MODE_CLICK_AND_HOLD,
      BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK: BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK,
      BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK_AND_HOLD: BUTTON_TRIGGER_MODE_CLICK_AND_DOUBLE_CLICK_AND_HOLD,
      BUTTON_TRIGGER_MODE_CLICK: BUTTON_TRIGGER_MODE_CLICK,
    };

    // proxy
    this.onScanResultFunction = this.onScanResult.bind(this);
    this.didReceiveButtonEventFunction = this.didReceiveButtonEvent.bind(this);
    this.onInitializedFunction = this.onInitialized.bind(this);

    // listen to events
    this.nativeEvents = new NativeEventEmitter(Flic2Module);

    // button click events
    this.nativeEvents.addListener('didReceiveButtonEvent', this.didReceiveButtonEventFunction);
    this.nativeEvents.addListener('managerInitialized', this.onInitializedFunction);

    // start the native context
    Flic2Module.startup();

    // known buttons
    this.knownButtons = {};

  }

  onInitialized() {

    this.isInitialized = true;

    // emit
    this.emit('managerInitialized');
  }

  isInitialized() {
    return this.isInitialized;
  }


  /**
   * Get a Flic2Button object by UUID.
   *
   * @version 1.0.0
   * @returns {Promise} Promise represents the Flic2Button object.
   */
  getButton(uuid) {
    return new Promise(async (resolve, reject) => {

      // first get all the buttons
      const buttons = await this.getButtons();

      // since the list of Flic buttons will never be huge
      // we do not bother with not using a loop here to find the button
      for (const button of buttons) {

        // check
        if (button.getUuid() === uuid) {

          return resolve(button);

        }

      }

    });
  }

  /**
   * Get an array of Flic2 Buttons.
   *
   * @version 1.0.0
   * @returns {Promise} Promise represents an array of Flic2Button objects.
   */
  getButtons() {

    return new Promise((resolve, reject) => {

      // proxy
      Flic2Module.getButtons((buttons) => {

        // mutate
        const exportButtons = [];
        for (const button of buttons){

          // button uuid
          const buttonUuid = button.uuid;

          // check if we have the button object in memory
          if (typeof this.knownButtons[buttonUuid] !== 'undefined'){

            // update
            const knownButton = this.knownButtons[buttonUuid];

            knownButton.setData(button);

            if (button.batteryLevelIsOk === false) {
              // emit
              this.emit('criticalBattery');
            }

            // add
            exportButtons.push(knownButton);

          } else {

            // create it
            this.knownButtons[buttonUuid] = new Flic2Button(button);

            if (button.batteryLevelIsOk === false) {
              // emit
              this.emit('criticalBattery');
            }

            // add
            exportButtons.push(this.knownButtons[buttonUuid]);

          }


        }

        // export all known buttons
        return resolve(exportButtons);

      }, reject);

    });
  }

  /**
   * Starts a scan to find a button.
   *
   * @version 1.0.0
   * @returns {boolean} Returns boolean true when finished.
   */
  startScan() {

    // listen for scanResult
    this.nativeEvents.addListener('scanResult', this.onScanResultFunction);

    // start the scan
    Flic2Module.startScan();

    // done
    return true;

  }

  /**
   * Stops the scan and removes the scanResult listener.
   *
   * @version 1.0.0
   * @returns {boolean} Returns boolean true when finished.
   */
  stopScan() {

    // listen for scanResult
    this.nativeEvents.removeListener('scanResult', this.onScanResultFunction);

    // start the scan
    Flic2Module.stopScan();

    // done
    return true;

  }

  /**
   * Event: scan result.
   * Emits a scanResult event to the react native application with a Flic2Button object attached.
   *
   * @version 1.0.0
   */
  onScanResult({ event, error, result, button }){

    // check if error
    let ButtonObject;
    if (event === 'completion') {
      // remove listener
      this.nativeEvents.removeListener('scanResult', this.onScanResultFunction);

      if (!!error === false) {

        // create button object
        ButtonObject = new Flic2Button(button);

        // save to known buttons
        this.knownButtons[ButtonObject.getUuid()] = ButtonObject;

      }
    } 

    // check if error
    this.emit('scanResult', {
      event: event,
      error: !!error,
      result: result,
      button: ButtonObject,
    });

  }

  /**
   * Event: did receive button event.
   * Emits the event we received from the button.
   *
   * @version 1.0.0
   */
  didReceiveButtonEvent(eventData){

    // button uuid
    const buttonUuid = eventData.button.uuid;

    // check if we have the button object in memory
    if (typeof this.knownButtons[buttonUuid] !== 'undefined'){

      // update
      const knownButton = this.knownButtons[buttonUuid];
      knownButton.setData(eventData.button);

      // proxy to button for individual listen events
      knownButton.didReceiveButtonEvent(eventData);

    } else {

      // create it
      this.knownButtons[buttonUuid] = new Flic2Button(eventData.button);

      // we do not call didReceiveButtonEvent because there could
      // not be any listeners at this point

    }

    // emit to application
    this.emit(eventData.event, {
      event: eventData.event,
      queued: eventData.queued,
      age: eventData.age,
      error: eventData.error,
      button: this.knownButtons[buttonUuid],
    });

  }



  connectAllKnownButtons() {

    // pass to native module
    Flic2Module.connectAllKnownButtons();

  }

  buttonConnect(uuid){
    return new Promise(resolve => {

      // pass to native module
      Flic2Module.connectButton(uuid, resolve);

    });
  }

  buttonForget(uuid) {
    return new Promise(resolve => {

      // remove from known list
      delete this.knownButtons[uuid];

      // pass to native module
      Flic2Module.forgetButton(uuid, resolve);

    });
  }

  buttonDisconnect(uuid) {
    return new Promise(resolve => {

      // pass to native module
      Flic2Module.disconnectButton(uuid, resolve);

    });
  }

  disconnectAllKnownButtons() {

    // pass to native module
    Flic2Module.disconnectAllKnownButtons();

  }

  forgetAllButtons() {

    // pass to native module
    Flic2Module.forgetAllButtons();
  }

  buttonSetName(uuid, name) {
    return new Promise(resolve => {

      // pass to native module
      Flic2Module.setName(uuid, name, resolve);

    });
  }

  buttonSetMode(uuid, mode) {
    return new Promise(resolve => {

      // ios only
      if (Platform.OS === 'ios') {

        // pass to native module
        Flic2Module.setMode(uuid, mode, resolve);

      } else {

        resolve();

      }


    });
  }

}

// export as singleton
export default new Flic2();
