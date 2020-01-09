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
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON                     = 3;
const SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED                           = 4;
const SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE                   = 5;
const SCAN_RESULT_ERROR_CONNECTION_TIMEOUT                                    = 6;
const SCAN_RESULT_ERROR_INVALID_VERIFIER                                      = 7;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING  = 8;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED                      = 9;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON                     = 10;
const SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH                            = 11;
const SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED                      = 12;
const SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS                             = 13;
const SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED                                  = 14;
const SCAN_RESULT_ERROR_TOO_MANY_APPS                                         = 15;
const SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY                        = 16;
const SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES                 = 17;
const SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION               = 18;
const SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH                                   = 19;
const SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED                              = 20;
const SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE                                    = 21;

/**
 * React Native Flic 2
 * Exposes the Flic2Module functions to the React Native context.
 *
 * @version 1.0.0
 */
class Flic2 extends EventEmitter {

  static get SCAN_RESULT_ERROR_ALREADY_RUNNING()                                      { return SCAN_RESULT_ERROR_ALREADY_RUNNING; }
  static get SCAN_RESULT_ERROR_BLUETOOTH_NOT_ACTIVATED()                              { return SCAN_RESULT_ERROR_BLUETOOTH_NOT_ACTIVATED; }
  static get SCAN_RESULT_ERROR_UNKNOWN()                                              { return SCAN_RESULT_ERROR_UNKNOWN; }
  static get SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED()                          { return SCAN_RESULT_ERROR_NO_PUBLIC_BUTTON_DISCOVERED; }
  static get SCAN_RESULT_ERROR_CONNECTION_TIMEOUT()                                   { return SCAN_RESULT_ERROR_CONNECTION_TIMEOUT; }
  static get SCAN_RESULT_ERROR_INVALID_VERIFIER()                                     { return SCAN_RESULT_ERROR_INVALID_VERIFIER; }
  static get SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED()                     { return SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_USER_CANCELED; }
  static get SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON()                    { return SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_UNKNOWN_REASON; }
  static get SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH()                           { return SCAN_RESULT_ERROR_APP_CREDENTIALS_DONT_MATCH; }
  static get SCAN_RESULT_ERROR_USER_CANCELED()                                        { return SCAN_RESULT_ERROR_USER_CANCELED; }
  static get SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS()                            { return SCAN_RESULT_ERROR_INVALID_BLUETOOTH_ADDRESS; }
  static get SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED()                                 { return SCAN_RESULT_ERROR_GENUINE_CHECK_FAILED; }
  static get SCAN_RESULT_ERROR_TOO_MANY_APPS()                                        { return SCAN_RESULT_ERROR_TOO_MANY_APPS; }
  static get SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY()                       { return SCAN_RESULT_ERROR_COULD_NOT_SET_BLUETOOTH_NOTIFY; }
  static get SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH()                                  { return SCAN_RESULT_ERROR_FAILED_TO_ESTABLISH; }
  static get SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED()                             { return SCAN_RESULT_ERROR_CONNECTION_LIMIT_REACHED; }
  static get SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE()                                   { return SCAN_RESULT_ERROR_NOT_IN_PUBLIC_MODE; }
  static get SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE()                  { return SCAN_RESULT_ERROR_ALREADY_CONNECTED_TO_ANOTHER_DEVICE; }
  static get SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING() { return SCAN_RESULT_ERROR_BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING; }
  static get SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES()                { return SCAN_RESULT_ERROR_COULD_NOT_DISCOVER_BLUETOOTH_SERVICES; }
  static get SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION()              { return SCAN_RESULT_ERROR_BUTTON_DISCONNECTED_DURING_VERIFICATION; }

  /**
   * Constructor.
   * 
   * @class
   * @version 1.0.0
   */
  constructor() {

    console.log({ NativeModules, Flic2Module })

    // extended class initialisation
    super();

    // start the native context
    Flic2Module.startup();

    // proxy
    this.onScanResultFunction = this.onScanResult.bind(this);

    // listen to events
    this.nativeEvents = new NativeEventEmitter(Flic2Module);

    // button click events
    this.nativeEvents.addListener('didReceiveButtonEvent', this.didReceiveButtonEvent);

    // known buttons
    this.knownButtons = {};

  }

  /**
   * Starts an Android service.
   * 
   * @version 1.0.0
   * @returns {boolean} Returns boolean true when finished.
   */
  startService() {

    // android only
    if(Platform.OS === 'android') {

      // proxy
      Flic2Module.startService();

    }

    // done
    return true;
  
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
      for(const button of buttons) {

        // check
        if(button.getUuid() === uuid) {

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

        console.log(buttons);
        // mutate
        const exportButtons = [];
        for(const button of buttons){
          console.log(button);
          // button uuid
          const buttonUuid = button.uuid;

          // check if we have the button object in memory
          if (typeof this.knownButtons[buttonUuid] !== 'undefined'){

            // update
            const knownButton = this.knownButtons[buttonUuid];
            knownButton.setData(button);

            // add
            exportButtons.push(knownButton);

          } else {

            // create it
            this.knownButtons[buttonUuid] = new Flic2Button(button);

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
  onScanResult({ error, result, button }){

    console.log('onscanresult',button)
    // remove listener
    this.nativeEvents.removeListener('scanResult', this.onScanResultFunction);

    // check if error
    let ButtonObject;
    if(error === false) {

      // create button object
      ButtonObject = new Flic2Button(button);

      // save to known buttons
      this.knownButtons[ButtonObject.getUuid()] = ButtonObject;

    }

    // check if error
    this.emit('scanResult', {
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
      knownButton.setData(event.button);

      // proxy to button for individual listen events
      knownButton.didReceiveButtonEvent(eventData);

    } else {

      console.log('didreceivebuttonevent', eventData)
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
      button: this.knownButtons[buttonUuid],
    });

  }



  connectAllKnownButtons() {

    // pass to native module
    Flic2Module.connectAllKnownButtons();
    
  }

  connectButton(uuid){
    return new Promise(resolve => {

      // pass to native module
      Flic2Module.connectButton(uuid, resolve);

    });
  }

  forgetButton(uuid) {
    return new Promise(resolve => {

      // remove from known list
      delete this.knownButtons[uuid];

      // pass to native module
      Flic2Module.forgetButton(uuid, resolve);

    });
  }

  disconnectButton(uuid) {
    return new Promise(resolve => {

      // pass to native module
      Flic2Module.disconnectButton(uuid, resolve);

    });
  }

  forgetAllButtons() {

    
  }

}

// export as singleton
export default new Flic2();
