import type { CodegenTypes } from 'react-native';
import Flic2, {
  type MultiplyEvent,
  type ManagerStateChangeEvent,
  type ScanStatusChangeEvent,
  type ButtonEvent,
  type FlicButton,
  type FlicManagerState,
  type FlicButtonState,
  type FlicTriggerMode,
  type FlicLatencyMode,
  type FlicScannerEvent,
} from './NativeFlic2';

// MARK: - Example Methods (kept for reference)

export function multiply(a: number, b: number): number {
  return Flic2.multiply(a, b);
}

export const onMultiply =
  Flic2.onMultiply as CodegenTypes.EventEmitter<MultiplyEvent>;

// MARK: - Manager Methods

export function initialize(
  background: boolean = false
): Promise<{ success: boolean; message: string }> {
  return Flic2.initialize(background);
}

export function getButtons(): Promise<FlicButton[]> {
  return Flic2.getButtons();
}

export function scanForButtons(): Promise<FlicButton> {
  return Flic2.scanForButtons();
}

export function stopScan(): Promise<{ success: boolean; message: string }> {
  return Flic2.stopScan();
}

export function forgetButton(
  uuid: string
): Promise<{ success: boolean; message: string }> {
  return Flic2.forgetButton(uuid);
}

// MARK: - Button Methods

export function connectButton(
  uuid: string
): Promise<{ success: boolean; message: string }> {
  return Flic2.connectButton(uuid);
}

export function disconnectButton(
  uuid: string
): Promise<{ success: boolean; message: string }> {
  return Flic2.disconnectButton(uuid);
}

export function setTriggerMode(
  uuid: string,
  mode: number
): Promise<{ success: boolean; message: string }> {
  return Flic2.setTriggerMode(uuid, mode);
}

export function setLatencyMode(
  uuid: string,
  mode: number
): Promise<{ success: boolean; message: string }> {
  return Flic2.setLatencyMode(uuid, mode);
}

export function setNickname(
  uuid: string,
  nickname: string
): Promise<{ success: boolean; message: string }> {
  return Flic2.setNickname(uuid, nickname);
}

// MARK: - Event Emitters

export const onManagerStateChange =
  Flic2.onManagerStateChange as CodegenTypes.EventEmitter<ManagerStateChangeEvent>;

export const onScanStatusChange =
  Flic2.onScanStatusChange as CodegenTypes.EventEmitter<ScanStatusChangeEvent>;

export const onButtonEvent =
  Flic2.onButtonEvent as CodegenTypes.EventEmitter<ButtonEvent>;

// MARK: - Type Exports

export type {
  MultiplyEvent,
  ManagerStateChangeEvent,
  ScanStatusChangeEvent,
  ButtonEvent,
  FlicButton,
  FlicManagerState,
  FlicButtonState,
  FlicTriggerMode,
  FlicLatencyMode,
  FlicScannerEvent,
};

// MARK: - Constants

export const TriggerMode = {
  ClickAndHold: 0,
  ClickAndDoubleClick: 1,
  ClickAndDoubleClickAndHold: 2,
  Click: 3,
} as const;

export const LatencyMode = {
  Normal: 0,
  Low: 1,
} as const;

export const ManagerState = {
  Unknown: 0,
  Resetting: 1,
  Unsupported: 2,
  Unauthorized: 3,
  PoweredOff: 4,
  PoweredOn: 5,
} as const;

export const ButtonState = {
  Disconnected: 0,
  Connecting: 1,
  Connected: 2,
  Disconnecting: 3,
} as const;
