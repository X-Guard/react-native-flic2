// event emitter
import EventEmitter from 'react-native/Libraries/vendor/emitter/EventEmitter';

// the Flic2 module
import Flic2 from 'react-native-flic2';

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
    this.batteryLevelIsOk = buttonData.batteryLevelIsOk;
    this.voltage = buttonData.voltage;
    this.pressCount = buttonData.pressCount;
    this.firmwareRevision = buttonData.firmwareRevision;
    this.isUnpaired = !!buttonData.isUnpaired;
    this.isReady = !!buttonData.isReady;
    this.serialNumber = buttonData.serial;

    // check unpaired
    if (this.isUnpaired === true) {

      // the session is no longer valid
      // Flic docs tell us to forget the button
      this.forget();

    }

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

    return Flic2.buttonConnect(this.uuid);

  }

  disconnect(){

    // proxy call to parent
    return Flic2.buttonDisconnect(this.uuid);

  }

  forget(){

    // proxy call to parent
    return Flic2.buttonForget(this.uuid);

  }

  setMode(mode){

    // proxy call to parent
    return Flic2.buttonSetMode(this.uuid, mode);

  }

  setName(name){

    // proxy call to parent
    return Flic2.buttonSetName(this.uuid, name);

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

  getBatteryLevelIsOk(){

    return this.batteryLevelIsOk;

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

  getIsReady(){

    return this.isReady;

  }

  getIsUnpaired(){

    return this.isUnpaired;

  }

  getSerialNumber(){

    return this.serialNumber;

  }

}

export default Flic2Button
;
