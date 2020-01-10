// event emitter
import EventEmitter from 'react-native/Libraries/vendor/emitter/EventEmitter';

// local
import Flic2 from './index.js';

/**
 * React Native Flic 2 Button object.
 *
 * @version 1.0.0
 */
class Flic2Button extends EventEmitter {

  /**
   * Constructor.
   * 
   * @class
   * @version 1.0.0
   */
  constructor(buttonData) {

    // extended class initialisation
    super();

    // initialise
    this.setData(buttonData);

    // initial isReady
    this.isReady = false;

    // listen for ready
    this.addListener('didReceiveConnectionReady', this.setReady.bind(this, true));
    this.addListener('buttonDisconnected', this.setReady.bind(this, false));

  }

  setData(buttonData) {

    // initialise
    this.uuid = buttonData.uuid;
    this.bluetoothAddress = buttonData.bluetoothAddress; 
    this.name = buttonData.name; 
    this.batteryLevel = buttonData.batteryLevel; 
    this.voltage = buttonData.voltage;
    this.pressCount = buttonData.pressCount; 
    this.firmwareRevision = buttonData.firmwareRevision;
    this.isUnpaired = !!buttonData.isUnpaired;

    // check unpaired
    if (this.isUnpaired === true) {

      // the session is no longer valid
      // Flic docs tell us to forget the button
      this.forget();

    }

  }

  setReady(isReady) {

    // initialise
    this.isReady = !!isReady;

  }

  /**
   * Event: did receive button event.
   * Emits the event we received from the button.
   * 
   * @version 1.0.0
   */
  didReceiveButtonEvent(eventData){

    // emit to application
    this.emit(eventData.event, {
      event: eventData.event,
      queued: eventData.queued,
      age: eventData.age,
      button: this,
    });

  }

  connect(){

    return Flic2.connectButton(this.uuid);

  }

  disconnect(){

    // proxy call to parent
    return Flic2.disconnectButton(this.uuid);

  }

  forget(){

    // proxy call to parent
    return Flic2.forgetButton(this.uuid);

  }

  setMode(mode){

    // proxy call to parent
    return Flic2.setMode(this.uuid, mode);

  }

  setName(name){

    // proxy call to parent
    return Flic2.setMode(this.uuid, name);

  }

  getUuid(){

    return this.uuid;

  }

  getBluetoothAddress(){

    return this.bluetoothAddress;

  }

  getName(){

    return this.name;

  }

  getBatteryLevel(){

    return this.batteryLevel;

  }

  getVoltage(){

    return this.voltage;

  }

  getPressCount(){

    return this.pressCount;

  }

  getFirmwareRevision(){

    return this.firmwareRevision;

  }

  isReady(){

    return this.isReady;

  }

  isUnpaired(){

    return this.isUnpaired;

  }

}

export default Flic2Button