// import type { CodegenTypes } from 'react-native';
// import type {
//   MultiplyEvent,
//   ManagerStateChangeEvent,
//   ScanStatusChangeEvent,
//   ButtonEvent,
//   FlicButton,
//   TriggerModeType,
//   LatencyModeType,
// } from './NativeFlic2';
// import NativeFlic2 from './NativeFlic2';

// // MARK: - Example Functions (keeping for compatibility)

// export function multiply(a: number, b: number): number {
//   return NativeFlic2.multiply(a, b);
// }

// export const onMultiply =
//   NativeFlic2.onMultiply as CodegenTypes.EventEmitter<MultiplyEvent>;

// // MARK: - Manager Functions

// export function initialize(
//   background: boolean
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.initialize(background);
// }

// export function getButtons(): Promise<FlicButton[]> {
//   return NativeFlic2.getButtons();
// }

// export function scanForButtons(): Promise<{
//   success: boolean;
//   message: string;
// }> {
//   return NativeFlic2.scanForButtons();
// }

// export function stopScan(): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.stopScan();
// }

// export function forgetButton(
//   uuid: string
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.forgetButton(uuid);
// }

// export function connectAllKnownButtons(): Promise<{
//   success: boolean;
//   message: string;
// }> {
//   return NativeFlic2.connectAllKnownButtons();
// }

// export function disconnectAllKnownButtons(): Promise<{
//   success: boolean;
//   message: string;
// }> {
//   return NativeFlic2.disconnectAllKnownButtons();
// }

// export function forgetAllButtons(): Promise<{
//   success: boolean;
//   message: string;
// }> {
//   return NativeFlic2.forgetAllButtons();
// }

// export function isScanning(): Promise<boolean> {
//   return NativeFlic2.isScanning();
// }

// // MARK: - Button Functions

// export function connectButton(
//   uuid: string
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.connectButton(uuid);
// }

// export function disconnectButton(
//   uuid: string
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.disconnectButton(uuid);
// }

// /**
//  * Sets the trigger mode for a button.
//  *
//  * @platform iOS
//  * @param uuid - Button UUID
//  * @param mode - Trigger mode (0-3)
//  * @returns Promise that resolves on iOS, rejects with NOT_SUPPORTED_ON_ANDROID on Android
//  *
//  * **Note:** This feature is only available on iOS. On Android, this will reject due to
//  * limitations in the Android Flic2 library v1.1.0+.
//  */
// export function setTriggerMode(
//   uuid: string,
//   mode: TriggerModeType
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.setTriggerMode(uuid, mode);
// }

// /**
//  * Sets the latency mode for a button.
//  *
//  * @platform iOS
//  * @param uuid - Button UUID
//  * @param mode - Latency mode (0-1)
//  * @returns Promise that resolves on iOS, rejects with NOT_SUPPORTED_ON_ANDROID on Android
//  *
//  * **Note:** This feature is only available on iOS. On Android, this will reject due to
//  * limitations in the Android Flic2 library v1.1.0+.
//  */
// export function setLatencyMode(
//   uuid: string,
//   mode: LatencyModeType
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.setLatencyMode(uuid, mode);
// }

// export function setNickname(
//   uuid: string,
//   nickname: string
// ): Promise<{ success: boolean; message: string }> {
//   return NativeFlic2.setNickname(uuid, nickname);
// }

// // MARK: - Event Emitters

// export const onManagerStateChange =
//   NativeFlic2.onManagerStateChange as CodegenTypes.EventEmitter<ManagerStateChangeEvent>;

// export const onScanStatusChange =
//   NativeFlic2.onScanStatusChange as CodegenTypes.EventEmitter<ScanStatusChangeEvent>;

// export const onButtonEvent =
//   NativeFlic2.onButtonEvent as CodegenTypes.EventEmitter<ButtonEvent>;

// // MARK: - Re-export Types

// export type {
//   MultiplyEvent,
//   ManagerStateChangeEvent,
//   ScanStatusChangeEvent,
//   ButtonEvent,
//   FlicButton,
//   FlicManagerState,
//   FlicButtonState,
//   FlicTriggerMode,
//   FlicLatencyMode,
//   FlicScannerEvent,
//   TriggerModeType,
//   LatencyModeType,
// } from './NativeFlic2';
