# react-native-flic2

React Native library for integrating Flic2 buttons into your React Native application. This library provides a complete interface to discover, connect, and interact with Flic2 buttons on both iOS and Android platforms.

## Features

- 🔍 Scan and discover Flic2 buttons
- 🔗 Connect and manage multiple buttons
- 📱 Receive button events (click, double click, hold)
- 🔋 Monitor battery status
- 🏷️ Set custom nicknames for buttons
- ⚙️ Configure trigger and latency modes
- 📡 Background connection support

> **⚠️ Important Notice**
>
> Parts of this project, including documentation and code examples, may have been generated with the assistance of AI tools and may contain errors. Please review all code and documentation carefully before use.
>
> This software is provided "AS IS" without warranty of any kind. Please refer to the [LICENSE](LICENSE) file for complete liability disclaimers. In no event shall the authors or copyright holders be liable for any claim, damages, or other liability arising from the use of this software.

## Installation

```sh
npm install react-native-flic2
```

### iOS Setup

1. **Install CocoaPods dependencies:**
   ```sh
   cd ios && pod install && cd ..
   ```

2. **Add Bluetooth permissions to `Info.plist`:**
   Add the following keys to your `ios/YourApp/Info.plist`:
   ```xml
   <key>NSBluetoothPeripheralUsageDescription</key>
   <string>This app needs Bluetooth to connect to Flic buttons</string>
   <key>NSBluetoothAlwaysUsageDescription</key>
   <string>This app needs Bluetooth to connect to Flic buttons in the background</string>
   ```

3. **Enable Background Modes:**
   - Open your project in Xcode
   - Select your app target
   - Go to "Signing & Capabilities"
   - Add "Background Modes" capability if not already added
   - Check "Uses Bluetooth LE accessories"

### Android Setup

The library automatically includes the necessary permissions in `AndroidManifest.xml`. However, you need to request runtime permissions in your app:

**For Android 12+ (API 31+):**
- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`

**For Android 11 and below:**
- `ACCESS_FINE_LOCATION`

You'll need to request these permissions before scanning for buttons. Use a library like `react-native-permissions` or implement permission requests manually.

## Basic Usage

### 1. Initialize the Library

First, initialize the Flic2 manager in your app. This should typically be done when your app starts:

```tsx
import Flic2 from 'react-native-flic2';

// Initialize the Flic2 manager
await Flic2.initialize();

// Connect to all previously known buttons
Flic2.connectAllKnownButtons();
```

### 2. Set Up Event Listeners

Listen to events from buttons and the manager:

```tsx
import Flic2, {
  ButtonEvent,
  ManagerStateChangeEvent,
  ScanStatusChangeEvent,
} from 'react-native-flic2';

// Listen for button events
Flic2.eventEmitter.on('buttonEvent', (event: ButtonEvent) => {
  console.log('Button event:', event.event, event.button.name);

  if (event.event === 'click') {
    // Handle single click
  } else if (event.event === 'doubleClick') {
    // Handle double click
  } else if (event.event === 'hold') {
    // Handle hold
  }
});

// Listen for manager state changes
Flic2.eventEmitter.on('managerStateChange', (event: ManagerStateChangeEvent) => {
  console.log('Manager state:', event.stateName);
});

// Listen for scan status changes
Flic2.eventEmitter.on('scanStatusChange', (event: ScanStatusChangeEvent) => {
  if (event.event === 'started') {
    console.log('Scan started');
  } else if (event.event === 'completion') {
    console.log('Scan completed');
  }
});
```

### 3. Complete Example

Here's a complete example component that demonstrates the main features:

```tsx
import React, { useState, useEffect } from 'react';
import { View, Text, Button, Alert, StyleSheet } from 'react-native';
import Flic2, {
  ButtonEvent,
  FlicButton,
  ManagerStateChangeEvent,
  ScanStatusChangeEvent,
} from 'react-native-flic2';

const Flic2Example = () => {
  const [buttons, setButtons] = useState<FlicButton[]>([]);
  const [isScanning, setIsScanning] = useState(false);

  useEffect(() => {
    // Initialize Flic2 when component mounts
    const initFlic2 = async () => {
      try {
        await Flic2.initialize();
        console.log('Flic2 initialized');

        // Connect to all known buttons
        Flic2.connectAllKnownButtons();

        // Load existing buttons
        loadButtons();
      } catch (error) {
        console.error('Failed to initialize Flic2:', error);
      }
    };

    initFlic2();

    // Set up event listeners
    const buttonSubscription = Flic2.eventEmitter.on(
      'buttonEvent',
      (event: ButtonEvent) => {
        console.log('Button event:', event.event, event.button.name);

        if (event.event === 'click' || event.event === 'doubleClick' || event.event === 'hold') {
          Alert.alert(
            event.button.nickname || event.button.name,
            `${event.event} at ${new Date().toLocaleTimeString()}`
          );
        }
      }
    );

    const scanSubscription = Flic2.eventEmitter.on(
      'scanStatusChange',
      (event: ScanStatusChangeEvent) => {
        console.log('Scan status:', event.event);

        if (event.event === 'started') {
          setIsScanning(true);
        } else if (event.event === 'completion') {
          setIsScanning(false);
          loadButtons(); // Refresh button list after scan
        }
      }
    );

    const managerSubscription = Flic2.eventEmitter.on(
      'managerStateChange',
      (event: ManagerStateChangeEvent) => {
        console.log('Manager state:', event.stateName);
      }
    );

    // Cleanup subscriptions on unmount
    return () => {
      buttonSubscription.remove();
      scanSubscription.remove();
      managerSubscription.remove();
    };
  }, []);

  const loadButtons = async () => {
    try {
      const buttonList = await Flic2.getButtons();
      setButtons(buttonList);
      console.log('Loaded buttons:', buttonList.length);
    } catch (error) {
      console.error('Failed to load buttons:', error);
    }
  };

  const startScan = async () => {
    try {
      // Check if already scanning
      const scanning = await Flic2.isScanning();
      if (scanning) {
        console.log('Already scanning');
        return;
      }

      // Request permissions here if needed (see Platform Setup)

      await Flic2.startScan();
      console.log('Scan started');
    } catch (error) {
      console.error('Failed to start scan:', error);
      Alert.alert('Error', 'Failed to start scanning for buttons');
    }
  };

  const stopScan = async () => {
    try {
      await Flic2.stopScan();
      console.log('Scan stopped');
    } catch (error) {
      console.error('Failed to stop scan:', error);
    }
  };

  const renameButton = (button: FlicButton) => {
    Alert.prompt(
      'Rename Button',
      'Enter a new name for the button',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Save',
          onPress: async (value) => {
            if (!value) return;
            try {
              await Flic2.buttonSetNickname(button.uuid, value);
              loadButtons(); // Refresh button list
            } catch (error) {
              console.error('Failed to rename button:', error);
            }
          },
        },
      ],
      'plain-text',
      button.nickname
    );
  };

  const forgetButton = (button: FlicButton) => {
    Alert.alert(
      'Delete Button',
      'Are you sure you want to delete this button?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await Flic2.forgetButton(button.uuid);
              loadButtons(); // Refresh button list
            } catch (error) {
              console.error('Failed to forget button:', error);
            }
          },
        },
      ]
    );
  };

  const showButtonOptions = (button: FlicButton) => {
    Alert.alert(
      'Button Options',
      button.nickname || button.name,
      [
        { text: 'Rename', onPress: () => renameButton(button) },
        { text: 'Delete', onPress: () => forgetButton(button) },
        { text: 'Cancel', style: 'cancel' },
      ]
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Flic2 Example</Text>

      <Button
        title={isScanning ? 'Stop Scanning' : 'Scan for Buttons'}
        onPress={isScanning ? stopScan : startScan}
      />

      <Button title="Refresh Buttons" onPress={loadButtons} />

      <Text style={styles.count}>
        Buttons: {buttons.length}
      </Text>

      {buttons.map((button) => (
        <View key={button.uuid} style={styles.buttonItem}>
          <Text style={styles.buttonName}>
            {button.nickname || button.name}
          </Text>
          <Text style={styles.buttonUuid}>{button.uuid}</Text>
          <Button
            title="Options"
            onPress={() => showButtonOptions(button)}
          />
        </View>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  count: {
    fontSize: 18,
    marginVertical: 10,
  },
  buttonItem: {
    padding: 10,
    marginVertical: 5,
    backgroundColor: '#f0f0f0',
    borderRadius: 5,
  },
  buttonName: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  buttonUuid: {
    fontSize: 12,
    color: '#666',
  },
});

export default Flic2Example;
```

## API Reference

### Initialization

#### `initialize(): Promise<boolean>`

Initialize the Flic2 manager. This must be called before using any other methods.

```tsx
await Flic2.initialize();
```

### Scanning

#### `startScan(): Promise<{ success: boolean; message: string }>`

Start scanning for new Flic2 buttons. The scan will emit `scanStatusChange` events.

```tsx
await Flic2.startScan();
```

#### `stopScan(): Promise<{ success: boolean; message: string }>`

Stop an ongoing scan.

```tsx
await Flic2.stopScan();
```

#### `isScanning(): Promise<boolean>`

Check if a scan is currently running.

```tsx
const scanning = await Flic2.isScanning();
```

### Button Management

#### `getButtons(): Promise<FlicButton[]>`

Get all known buttons.

```tsx
const buttons = await Flic2.getButtons();
```

#### `getButton(uuid: string): Promise<FlicButton | null>`

Get a specific button by UUID.

```tsx
const button = await Flic2.getButton('button-uuid');
```

#### `connectAllKnownButtons(): Promise<{ success: boolean; message: string }>`

Connect to all previously known buttons.

```tsx
await Flic2.connectAllKnownButtons();
```

#### `disconnectAllKnownButtons(): Promise<{ success: boolean; message: string }>`

Disconnect all connected buttons.

```tsx
await Flic2.disconnectAllKnownButtons();
```

#### `forgetButton(uuid: string): Promise<{ success: boolean; message: string }>`

Forget (unpair) a specific button.

```tsx
await Flic2.forgetButton('button-uuid');
```

#### `forgetAllButtons(): Promise<{ success: boolean; message: string }>`

Forget all buttons.

```tsx
await Flic2.forgetAllButtons();
```

### Button Configuration

#### `buttonConnect(uuid: string): Promise<{ success: boolean; message: string }>`

Connect to a specific button.

```tsx
await Flic2.buttonConnect('button-uuid');
```

#### `buttonDisconnect(uuid: string): Promise<{ success: boolean; message: string }>`

Disconnect a specific button.

```tsx
await Flic2.buttonDisconnect('button-uuid');
```

#### `buttonSetNickname(uuid: string, nickname: string): Promise<{ success: boolean; message: string }>`

Set a custom nickname for a button.

```tsx
await Flic2.buttonSetNickname('button-uuid', 'My Button');
```

#### `buttonSetTriggerMode(uuid: string, mode: TriggerModeType): Promise<{ success: boolean; message: string }>`

Set the trigger mode for a button. Modes:
- `0`: Click and Hold
- `1`: Click and Double Click
- `2`: Click and Double Click and Hold
- `3`: Click only

```tsx
await Flic2.buttonSetTriggerMode('button-uuid', 3); // Click only
```

#### `buttonSetLatencyMode(uuid: string, mode: LatencyModeType): Promise<{ success: boolean; message: string }>`

Set the latency mode for a button. Modes:
- `0`: Normal latency
- `1`: Low latency

```tsx
await Flic2.buttonSetLatencyMode('button-uuid', 1); // Low latency
```

#### `getBatteryHealth(uuid: string): Promise<boolean>`

Get the battery health status of a button. Returns `true` if battery voltage is above 2.65V.

```tsx
const isHealthy = await Flic2.getBatteryHealth('button-uuid');
```

## Events

The library uses an event emitter pattern to notify your app of button events and state changes.

### `buttonEvent`

Emitted when a button event occurs (click, double click, hold, connection, etc.).

```tsx
Flic2.eventEmitter.on('buttonEvent', (event: ButtonEvent) => {
  console.log('Event:', event.event);
  console.log('Button:', event.button.name);
  console.log('UUID:', event.uuid);
});
```

**Event Types:**
- `'discovered'` - Button was discovered during scan
- `'connected'` - Button connected successfully
- `'ready'` - Button is ready to receive events
- `'disconnected'` - Button disconnected
- `'connectionFailed'` - Connection attempt failed
- `'buttonDown'` - Button was pressed down
- `'buttonUp'` - Button was released
- `'click'` - Single click detected
- `'doubleClick'` - Double click detected
- `'hold'` - Button held down
- `'unpaired'` - Button was unpaired
- `'batteryUpdate'` - Battery status updated
- `'nicknameUpdate'` - Nickname was updated

**Event Object:**
```tsx
type ButtonEvent = {
  uuid: string;
  event: string;
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
```

### `managerStateChange`

Emitted when the Flic2 manager state changes (Bluetooth state, etc.).

```tsx
Flic2.eventEmitter.on('managerStateChange', (event: ManagerStateChangeEvent) => {
  console.log('State:', event.stateName);
  console.log('State code:', event.state);
});
```

**Event Object:**
```tsx
type ManagerStateChangeEvent = {
  event: 'restored' | 'stateChanged';
  state?: number;
  stateName?: string;
  message?: string;
};
```

**State Names:**
- `'unknown'`
- `'resetting'`
- `'unsupported'`
- `'unauthorized'`
- `'poweredOff'`
- `'poweredOn'`

### `scanStatusChange`

Emitted when the scan status changes.

```tsx
Flic2.eventEmitter.on('scanStatusChange', (event: ScanStatusChangeEvent) => {
  if (event.event === 'started') {
    console.log('Scan started');
  } else if (event.event === 'completion') {
    console.log('Scan completed', event.result);
  }
});
```

**Event Object:**
```tsx
type ScanStatusChangeEvent = {
  event: 'started' | 'completion';
  eventName: 'started' | 'completion';
  result?: ScanResult;
};
```

## Types

### `FlicButton`

```tsx
type FlicButton = {
  uuid: string;
  identifier: string;
  name: string;
  nickname: string;
  bluetoothAddress: string;
  serialNumber: string;
  state: number;
  stateName: string;
  triggerMode: number; // iOS only, 0 on Android
  triggerModeName: string; // iOS only, empty on Android
  latencyMode: number; // iOS only, 0 on Android
  latencyModeName: string; // iOS only, empty on Android
  pressCount: number;
  firmwareRevision: number;
  isReady: boolean;
  batteryVoltage: number;
  isUnpaired: boolean;
};
```

## Common Use Cases

### Connecting to Buttons on App Start

```tsx
useEffect(() => {
  const init = async () => {
    await Flic2.initialize();
    Flic2.connectAllKnownButtons();
  };
  init();
}, []);
```

### Handling Button Clicks

```tsx
Flic2.eventEmitter.on('buttonEvent', (event) => {
  if (event.event === 'click') {
    // Handle single click
    console.log('Button clicked:', event.button.name);
  } else if (event.event === 'doubleClick') {
    // Handle double click
    console.log('Button double clicked:', event.button.name);
  } else if (event.event === 'hold') {
    // Handle hold
    console.log('Button held:', event.button.name);
  }
});
```

### Monitoring Battery Status

```tsx
Flic2.eventEmitter.on('buttonEvent', (event) => {
  if (event.event === 'batteryUpdate') {
    const isHealthy = event.batteryVoltageOk;
    console.log('Battery healthy:', isHealthy);
    console.log('Battery voltage:', event.voltage);
  }
});
```

### Scanning for New Buttons

```tsx
const scanForButtons = async () => {
  // Request permissions first (see Platform Setup)

  Flic2.eventEmitter.on('scanStatusChange', (event) => {
    if (event.event === 'completion') {
      if (event.result === 0) { // ScanResult.SUCCESS
        console.log('Button found and paired!');
        loadButtons();
      } else {
        console.log('Scan failed:', event.result);
      }
    }
  });

  await Flic2.startScan();
};
```

## Troubleshooting

### Buttons not connecting

- Ensure Bluetooth is enabled on the device
- Check that you've requested the necessary permissions
- Verify the button is in pairing mode (press and hold for 7 seconds)
- Make sure the button isn't already connected to another device

### Scan not starting

- Verify runtime permissions are granted (especially location permission on Android)
- Check that Bluetooth is enabled
- Ensure you're not already scanning (check with `isScanning()`)

### Events not firing

- Make sure you've called `initialize()` before setting up event listeners
- Verify the button is connected and ready (`button.isReady === true`)
- Check that the button's trigger mode supports the event you're listening for

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
