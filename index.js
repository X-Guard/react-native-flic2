// get the native module
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

// event emitter
import EventEmitter from 'react-native/Libraries/vendor/emitter/EventEmitter';

// local imports
import Flic2Button from './flic2Button.js';

// we only care about flic 2 here
const { Flic2 } = NativeModules;

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

    console.log({ NativeModules, Flic2Module })

    // extended class initialisation
    super();

    // start the native context
    Flic2Module.startup();

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
      Flic2Module.getButtons(resolve, reject);

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
    this.nativeEvents.addListener('scanResult', this.onScanResult);

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
    this.nativeEvents.removeListener('scanResult', this.onScanResult);

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
  onScanResult(error, result, button){

    // remove listener
    this.nativeEvents.removeListener('scanResult', this.onScanResult);

    // check if error
    let ButtonObject;
    if(error === false) {

      // create button object
      ButtonObject = new Flic2Button(button);

      // save to known buttons
      this.knownButtons[ButtonObject.getUuid()] = ButtonObject;

    }

    // check if error
    this.emit('scanResult', error, result, ButtonObject);

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
