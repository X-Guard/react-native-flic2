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
    managerInitialized: () => void;
  }>;

  /**
   * Constructor.
   *
   * @class
   * @version 2.0.0
   */
  constructor(options: { background: boolean; autoStartUp: boolean }) {
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

    // start up the Flic2 manager if autoStartUp is true
    if (options.autoStartUp !== false) {
      this.startup({
        background: options.background ?? true,
      });
    }
  }

  // MARK: Public management methods
  /**
   * Start up the Flic2 manager.
   *
   * @param options - The options for the Flic2 manager.
   * @param options.background - Whether to run the Flic2 manager in the background.
   * @returns A promise that resolves when the Flic2 manager is started up.
   */
  public async startup(options: { background: boolean }): Promise<void> {
    // check if the Flic2 manager is already initialized
    if (this.isInitialized()) {
      throw new Error('Flic2 manager is already initialized');
    }

    // initialize the Flic2 manager
    const result = await NativeFlic2.initialize(options.background);

    if (!result.success) {
      throw new Error(result.message);
    }

    this.onInitialized();
  }

  /**
   * Scan for buttons.
   *
   * @returns A promise that resolves when the scan is started. Events will be emitted for the scan process.
   */
  public scanForButtons(): Promise<{ success: boolean; message: string }> {
    return NativeFlic2.scanForButtons();
  }

  /**
   * Called when the Flic2 manager is initialized.
   */
  public onInitialized(): void {
    this.isFlic2ManagerInitialized = true;
    this.eventEmitter.emit('managerInitialized');
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
  public connectAllKnownButtons(): Promise<{
    success: boolean;
    message: string;
  }> {
    return NativeFlic2.connectAllKnownButtons();
  }

  /**
   * Disconnect all known buttons.
   *
   * @returns A promise that resolves when the all known buttons are disconnected.
   */
  public disconnectAllKnownButtons(): Promise<{
    success: boolean;
    message: string;
  }> {
    return NativeFlic2.disconnectAllKnownButtons();
  }

  /**
   * Forget all buttons.
   *
   * @returns A promise that resolves when the all buttons are forgotten.
   */
  public forgetAllButtons(): Promise<{ success: boolean; message: string }> {
    return NativeFlic2.forgetAllButtons();
  }

  // MARK: Public button methods
  /**
   * Connect a button.
   *
   * @param uuid - The UUID of the button to connect.
   * @returns A promise that resolves when the button is connected.
   */
  public buttonConnect(
    uuid: string
  ): Promise<{ success: boolean; message: string }> {
    return NativeFlic2.connectButton(uuid);
  }

  /**
   * Disconnect a button.
   *
   * @param uuid - The UUID of the button to disconnect.
   * @returns A promise that resolves when the button is disconnected.
   */
  public buttonDisconnect(
    uuid: string
  ): Promise<{ success: boolean; message: string }> {
    return NativeFlic2.disconnectButton(uuid);
  }

  /**
   * Set the trigger mode of a button.
   *
   * @param uuid - The UUID of the button to set the trigger mode of.
   * @param mode - The trigger mode to set.
   * @returns A promise that resolves when the trigger mode is set.
   */
  public buttonSetTriggerMode(
    uuid: string,
    mode: TriggerModeType
  ): Promise<{ success: boolean; message: string }> {
    return NativeFlic2.setTriggerMode(uuid, mode);
  }

  /**
   * Set the latency mode of a button.
   *
   * @param uuid - The UUID of the button to set the latency mode of.
   * @param mode - The latency mode to set.
   * @returns A promise that resolves when the latency mode is set.
   */
  public buttonSetLatencyMode(
    uuid: string,
    mode: LatencyModeType
  ): Promise<{ success: boolean; message: string }> {
    return NativeFlic2.setLatencyMode(uuid, mode);
  }

  /**
   * Set the nickname of a button.
   *
   * @param uuid - The UUID of the button to set the nickname of.
   * @param nickname - The nickname to set.
   * @returns A promise that resolves when the nickname is set.
   */
  public buttonSetNickname(
    uuid: string,
    nickname: string
  ): Promise<{ success: boolean; message: string }> {
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
    const button = buttons.find((button: FlicButton) => button.uuid === uuid);

    return button ?? null;
  }

  // MARK: Private Methods
  /**
   * Called when a button event is received from the native side.
   *
   * @param event - The button event.
   */
  private onNativeButtonEvent(event: ButtonEvent): void {
    this.eventEmitter.emit('buttonEvent', event);
  }

  /**
   * Called when a manager state change event is received from the native side.
   *
   * @param event - The manager state change event.
   */
  private onNativeManagerStateChange(event: ManagerStateChangeEvent): void {
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
export default new Flic2({ background: true, autoStartUp: true });

// re-export types
export type * from './NativeFlic2';
