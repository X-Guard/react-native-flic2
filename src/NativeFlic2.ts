import {
  TurboModuleRegistry,
  type TurboModule,
  type CodegenTypes,
} from 'react-native';

// MARK: - Event Types

export type ManagerStateChangeEvent = {
  event: 'restored' | 'stateChanged';
  state?: number;
  stateName?: string;
  message?: string;
};

export enum ScanResult {
  SUCCESS = 0,
  ALREADY_RUNNING = 1,
  BLUETOOTH_NOT_ACTIVATED = 2,
  UNKNOWN = 3,
  NO_PUBLIC_BUTTON_DISCOVERED = 4,
  ALREADY_CONNECTED_TO_ANOTHER_DEVICE = 5,
  CONNECTION_TIMEOUT = 6,
  INVALID_VERIFIER = 7,
  BLE_PAIRING_FAILED_PREVIOUS_PAIRING_ALREADY_EXISTING = 8,
  BLE_PAIRING_FAILED_USER_CANCELED = 9,
  BLE_PAIRING_FAILED_UNKNOWN_REASON = 10,
  APP_CREDENTIALS_DONT_MATCH = 11,
  USER_CANCELED = 12,
  INVALID_BLUETOOTH_ADDRESS = 13,
  GENUINE_CHECK_FAILED = 14,
  TOO_MANY_APPS = 15,
  COULD_NOT_SET_BLUETOOTH_NOTIFY = 16,
  COULD_NOT_DISCOVER_BLUETOOTH_SERVICES = 17,
  BUTTON_DISCONNECTED_DURING_VERIFICATION = 18,
  FAILED_TO_ESTABLISH = 19,
  CONNECTION_LIMIT_REACHED = 20,
  NOT_IN_PUBLIC_MODE = 21,
}

export type ScanStatus = 'started' | 'completion';

export type ScanStatusChangeEvent = {
  event: ScanStatus;
  eventName: ScanStatus;
  result?: ScanResult;
};

export type ButtonEventName =
  | 'discovered'
  | 'connected'
  | 'ready'
  | 'disconnected'
  | 'connectionFailed'
  | 'buttonDown'
  | 'buttonUp'
  | 'click'
  | 'doubleClick'
  | 'hold'
  | 'unpaired'
  | 'batteryUpdate'
  | 'nicknameUpdate';

export type ButtonEvent = {
  uuid: string;
  event: ButtonEventName;
  queued?: boolean;
  age?: number;
  nickname?: string;
  voltage?: number;
  batteryVoltageOk?: boolean;
  error?: {
    code: number;
    message: string;
  };
  button: FlicButton;
};

// MARK: - Flic2 Types

export type FlicButton = {
  uuid: string;
  identifier: string;
  name: string;
  nickname: string;
  bluetoothAddress: string;
  serialNumber: string;
  state: number;
  stateName: string;
  /** @platform iOS - Returns 0 on Android */
  triggerMode: number;
  /** @platform iOS - Returns empty string on Android */
  triggerModeName: string;
  /** @platform iOS - Returns 0 on Android */
  latencyMode: number;
  /** @platform iOS - Returns empty string on Android */
  latencyModeName: string;
  pressCount: number;
  firmwareRevision: number;
  isReady: boolean;
  batteryVoltage: number;
  isUnpaired: boolean;
};

export type FlicManagerState =
  | 'unknown'
  | 'resetting'
  | 'unsupported'
  | 'unauthorized'
  | 'poweredOff'
  | 'poweredOn';

export type FlicButtonState =
  | 'disconnected'
  | 'connecting'
  | 'connected'
  | 'disconnecting';

export type FlicTriggerMode =
  | 'clickAndHold'
  | 'clickAndDoubleClick'
  | 'clickAndDoubleClickAndHold'
  | 'click';

export type FlicLatencyMode = 'normal' | 'low';

// MARK: - Mode Types

export type TriggerModeType = 0 | 1 | 2 | 3;
export type LatencyModeType = 0 | 1;

// MARK: - Spec Interface

export interface Spec extends TurboModule {
  // Manager methods
  initialize(
    background: boolean
  ): Promise<{ success: true; message: string }>;
  getButtons(): Promise<FlicButton[]>;
  scanForButtons(): Promise<{ success: true; message: string }>;
  stopScan(): Promise<{ success: true; message: string }>;
  forgetButton(uuid: string): Promise<{ success: true; message: string }>;
  connectAllKnownButtons(): Promise<{ success: true; message: string }>;
  disconnectAllKnownButtons(): Promise<{ success: true; message: string }>;
  forgetAllButtons(): Promise<{ success: true; message: string }>;
  isScanning(): Promise<boolean>;

  // Button methods
  connectButton(uuid: string): Promise<{ success: true; message: string; button: FlicButton }>;
  disconnectButton(
    uuid: string
  ): Promise<{ success: true; message: string; button: FlicButton }>;
  setTriggerMode(
    uuid: string,
    mode: TriggerModeType
  ): Promise<{ success: true; message: string; button: FlicButton }>;
  setLatencyMode(
    uuid: string,
    mode: LatencyModeType
  ): Promise<{ success: true; message: string; button: FlicButton }>;
  setNickname(
    uuid: string,
    nickname: string
  ): Promise<{ success: true; message: string; button: FlicButton }>;

  // Event emitters
  readonly onManagerStateChange: CodegenTypes.EventEmitter<ManagerStateChangeEvent>;
  readonly onScanStatusChange: CodegenTypes.EventEmitter<ScanStatusChangeEvent>;
  readonly onButtonEvent: CodegenTypes.EventEmitter<ButtonEvent>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Flic2');
