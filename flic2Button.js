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

}

export default Flic2Button