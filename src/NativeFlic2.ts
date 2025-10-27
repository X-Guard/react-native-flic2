import {
  TurboModuleRegistry,
  type TurboModule,
  type CodegenTypes,
} from 'react-native';

// MARK: - Event Types

export type MultiplyEvent = {
  a: number;
  b: number;
  result: number;
};

export type ManagerStateChangeEvent = {
  event: 'restored' | 'stateChanged';
  state?: number;
  stateName?: string;
  message?: string;
};

export type ScanStatusChangeEvent = {
  event: number;
  eventName: string;
};

export type ButtonEvent = {
  uuid: string;
  event:
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
  queued?: boolean;
  age?: number;
  voltage?: number;
  nickname?: string;
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

export type FlicScannerEvent =
  | 'discovered'
  | 'connected'
  | 'verified'
  | 'verificationFailed';

// MARK: - Mode Types

export type TriggerModeType = 0 | 1 | 2 | 3;
export type LatencyModeType = 0 | 1;

// MARK: - Spec Interface

export interface Spec extends TurboModule {
  // Keep multiply example
  multiply(a: number, b: number): number;
  readonly onMultiply: CodegenTypes.EventEmitter<MultiplyEvent>;

  // Manager methods
  initialize(
    background: boolean
  ): Promise<{ success: boolean; message: string }>;
  getButtons(): Promise<FlicButton[]>;
  scanForButtons(): Promise<{ success: boolean; message: string }>;
  stopScan(): Promise<{ success: boolean; message: string }>;
  forgetButton(uuid: string): Promise<{ success: boolean; message: string }>;
  connectAllKnownButtons(): Promise<{ success: boolean; message: string }>;
  disconnectAllKnownButtons(): Promise<{ success: boolean; message: string }>;
  forgetAllButtons(): Promise<{ success: boolean; message: string }>;
  isScanning(): Promise<boolean>;

  // Button methods
  connectButton(uuid: string): Promise<{ success: boolean; message: string }>;
  disconnectButton(
    uuid: string
  ): Promise<{ success: boolean; message: string }>;
  setTriggerMode(
    uuid: string,
    mode: TriggerModeType
  ): Promise<{ success: boolean; message: string }>;
  setLatencyMode(
    uuid: string,
    mode: LatencyModeType
  ): Promise<{ success: boolean; message: string }>;
  setNickname(
    uuid: string,
    nickname: string
  ): Promise<{ success: boolean; message: string }>;

  // Event emitters
  readonly onManagerStateChange: CodegenTypes.EventEmitter<ManagerStateChangeEvent>;
  readonly onScanStatusChange: CodegenTypes.EventEmitter<ScanStatusChangeEvent>;
  readonly onButtonEvent: CodegenTypes.EventEmitter<ButtonEvent>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Flic2');
