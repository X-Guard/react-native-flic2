import { TypedEmitter } from './lib/typedEventEmitter';
import NativeFlic2, {
  type ButtonEvent,
  type FlicButton,
  type LatencyModeType,
  type ManagerStateChangeEvent,
  type ScanStatusChangeEvent,
  type TriggerModeType,
} from './NativeFlic2';

class Flic2 {

  private isFlic2ManagerInitialized: boolean = false;

  public eventEmitter: TypedEmitter<{
    buttonEvent: (event: ButtonEvent) => void;
    managerStateChange: (event: ManagerStateChangeEvent) => void;
    scanStatusChange: (event: ScanStatusChangeEvent) => void;
  }>;

  /**
   * Constructor.
   *
   * @class
   * @version 2.0.0
   */
  constructor() {

    // create event emitter
    this.eventEmitter = new TypedEmitter<{
      buttonEvent: (event: ButtonEvent) => void;
      managerStateChange: (event: ManagerStateChangeEvent) => void;
      scanStatusChange: (event: ScanStatusChangeEvent) => void;
    }>();

    // listen
    NativeFlic2.onButtonEvent(this.onNativeButtonEvent.bind(this));
    NativeFlic2.onManagerStateChange(
      this.onNativeManagerStateChange.bind(this)
    );

    NativeFlic2.onScanStatusChange(this.onNativeScanStatusChange.bind(this));

  }

  // MARK: Public management methods
  /**
   * Initialize the Flic2 manager.
   *
   * @returns A promise that resolves when the Flic2 manager is initialized.
   */
  public async initialize(): Promise<void> {

    // check if the Flic2 manager is already initialized
    if (this.isInitialized()) {

      throw new Error('Flic2 manager is already initialized');

    }

    // initialize the Flic2 manager in background
    await NativeFlic2.initialize(true);

    this.onInitialized();

  }

  /**
   * Scan for buttons.
   *
   * @returns A promise that resolves when the scan is started. Events will be emitted for the scan process.
   */
  public startScan(): Promise<void> {

    return NativeFlic2.scanForButtons();

  }

  /**
   * Stop the scan.
   *
   * @returns A promise that resolves when the scan is stopped.
   */
  public stopScan(): Promise<void> {

    return NativeFlic2.stopScan();

  }

  /**
   * Called when the Flic2 manager is initialized.
   */
  public onInitialized(): void {

    this.isFlic2ManagerInitialized = true;

  }

  /**
   * Check if the Flic2 manager is initialized.
   *
   * @returns True if the Flic2 manager is initialized, false otherwise.
   */
  public isInitialized(): boolean {

    return this.isFlic2ManagerInitialized;

  }

  /**
   * Connect all known buttons.
   *
   * @returns A promise that resolves when the all known buttons are connected.
   */
  public connectAllKnownButtons(): Promise<void> {

    return NativeFlic2.connectAllKnownButtons();

  }

  /**
   * Disconnect all known buttons.
   *
   * @returns A promise that resolves when the all known buttons are disconnected.
   */
  public disconnectAllKnownButtons(): Promise<void> {

    return NativeFlic2.disconnectAllKnownButtons();

  }

  /**
   * Forget all buttons.
   *
   * @returns A promise that resolves when the all buttons are forgotten.
   */
  public forgetAllButtons(): Promise<void> {

    return NativeFlic2.forgetAllButtons();

  }

  /**
   * Check if a scan is currently running.
   *
   * @returns A promise that resolves to true if scanning, false otherwise.
   */
  public isScanning(): Promise<boolean> {

    return NativeFlic2.isScanning();

  }

  /**
   * Forget a specific button by UUID.
   *
   * @param uuid - The UUID of the button to forget.
   * @returns A promise that resolves when the button is forgotten.
   */
  public forgetButton(
    uuid: string
  ): Promise<void> {

    return NativeFlic2.forgetButton(uuid);

  }

  // MARK: Public button methods
  /**
   * Connect a button.
   *
   * @param uuid - The UUID of the button to connect.
   * @returns A promise that resolves with the button when the connection is initiated.
   */
  public buttonConnect(
    uuid: string
  ): Promise<FlicButton> {

    return NativeFlic2.connectButton(uuid);

  }

  /**
   * Disconnect a button.
   *
   * @param uuid - The UUID of the button to disconnect.
   * @returns A promise that resolves with the button when the disconnection is initiated.
   */
  public buttonDisconnect(
    uuid: string
  ): Promise<FlicButton> {

    return NativeFlic2.disconnectButton(uuid);

  }

  /**
   * Set the trigger mode of a button.
   *
   * @param uuid - The UUID of the button to set the trigger mode of.
   * @param mode - The trigger mode to set.
   * @returns A promise that resolves with the button when the trigger mode is set.
   */
  public buttonSetTriggerMode(
    uuid: string,
    mode: TriggerModeType
  ): Promise<FlicButton> {

    return NativeFlic2.setTriggerMode(uuid, mode);

  }

  /**
   * Set the latency mode of a button.
   *
   * @param uuid - The UUID of the button to set the latency mode of.
   * @param mode - The latency mode to set.
   * @returns A promise that resolves with the button when the latency mode is set.
   */
  public buttonSetLatencyMode(
    uuid: string,
    mode: LatencyModeType
  ): Promise<FlicButton> {

    return NativeFlic2.setLatencyMode(uuid, mode);

  }

  /**
   * Set the nickname of a button.
   *
   * @param uuid - The UUID of the button to set the nickname of.
   * @param nickname - The nickname to set.
   * @returns A promise that resolves with the button when the nickname is set.
   */
  public buttonSetNickname(
    uuid: string,
    nickname: string
  ): Promise<FlicButton> {

    return NativeFlic2.setNickname(uuid, nickname);

  }

  /**
   * Get all buttons.
   *
   * @returns A promise that resolves with an array of FlicButton instances.
   */
  public getButtons(): Promise<FlicButton[]> {

    return NativeFlic2.getButtons();

  }

  /**
   * Get a button by UUID.
   *
   * @param uuid - The UUID of the button to get.
   * @returns A promise that resolves with the FlicButton instance or null if the button is not found.
   */
  public async getButton(uuid: string): Promise<FlicButton | null> {

    const buttons = await NativeFlic2.getButtons();
    const button = buttons.find((item: FlicButton) => item.uuid === uuid);

    return button ?? null;

  }

  /**
   * Get the battery health status of a button.
   *
   * @param uuid - The UUID of the button to check.
   * @returns A promise that resolves to true if battery is OK (voltage > 2.65V), false otherwise.
   * @throws Error if the button is not found.
   */
  public async getBatteryHealth(uuid: string): Promise<boolean> {

    const button = await this.getButton(uuid);

    if (!button) {

      throw new Error(`Button with UUID ${uuid} not found`);

    }

    return this.isBatteryVoltageOk(button.batteryVoltage);

  }

  // MARK: Private Methods
  /**
   * Check if battery voltage is OK based on the 2.65V threshold.
   *
   * @param voltage - The battery voltage in volts.
   * @returns True if voltage is above 2.65V (battery is OK), false otherwise.
   */
  private isBatteryVoltageOk(voltage: number): boolean {

    return voltage * 1000 > 2650;

  }

  /**
   * Called when a button event is received from the native side.
   *
   * @param event - The button event.
   */
  private onNativeButtonEvent(event: ButtonEvent): void {

    // Enrich batteryUpdate events with batteryVoltageOk
    if (event.event === 'batteryUpdate') {

      const voltage = typeof event.voltage === 'number' ? event.voltage : event.button?.batteryVoltage;

      const enrichedEvent: ButtonEvent = {
        ...event,
        batteryVoltageOk: this.isBatteryVoltageOk(voltage ?? 0),
      };

      this.eventEmitter.emit('buttonEvent', enrichedEvent);

      return;

    }

    this.eventEmitter.emit('buttonEvent', event);

  }

  /**
   * Called when a manager state change event is received from the native side.
   *
   * @param event - The manager state change event.
   */
  private onNativeManagerStateChange(event: ManagerStateChangeEvent): void {

    // When manager is restored, mark it as initialized
    if (event.event === 'restored' && !this.isFlic2ManagerInitialized) {

      this.onInitialized();

    }

    this.eventEmitter.emit('managerStateChange', event);

  }

  /**
   * Called when a scan status change event is received from the native side.
   *
   * @param event - The scan status change event.
   */
  private onNativeScanStatusChange(event: ScanStatusChangeEvent): void {

    this.eventEmitter.emit('scanStatusChange', event);

  }

}

// export as singleton
export default new Flic2();

// re-export types
export type * from './NativeFlic2';
