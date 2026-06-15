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

## Version Information

**This is a complete rewrite of react-native-flic2 (version 2.x.x).** This version requires React Native 0.81.x or higher. We do not provide a breaking changes list or migration guide. If you are upgrading from version 1.x.x, you should restart your implementation based on the examples and documentation provided in this README. The API and architecture have been completely redesigned.

If you need support for older React Native versions, please use version 1.x.x of this package instead.

## Installation

```sh
npm install react-native-flic2
```

### iOS Setup

1. **Install CocoaPods dependencies:**
   ```sh
   cd ios && pod install && cd ..
   ```

   **Need simulator builds?**
   See `Troubleshooting` -> `Running on iOS Simulator` for a full configuration
   example and launch commands.

   **Important (iOS pod wiring):**
   The autolinked root `Flic2` pod is codegen-only in the 2.x beta setup.
   To get a real iOS runtime implementation, add `Flic2Device` in your Podfile
   (and `Flic2Simulator` for simulator-specific configurations).

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

#### Customizing the Foreground Service Notification (Android)

The library runs a foreground service to keep Flic2 buttons connected in the background. You can customize the notification appearance by adding metadata to your app's `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <!-- Your existing application configuration -->

        <!-- Customize Flic2 foreground service notification -->
        <meta-data
            android:name="nl.xguard.flic2.notification_title"
            android:value="My Flic2 Service" />
        <meta-data
            android:name="nl.xguard.flic2.notification_text"
            android:value="Flic2 buttons are active" />
        <meta-data
            android:name="nl.xguard.flic2.notification_icon"
            android:resource="@drawable/ic_notification" />
        <meta-data
            android:name="nl.xguard.flic2.notification_channel_name"
            android:value="Flic2 Notifications" />
        <meta-data
            android:name="nl.xguard.flic2.notification_channel_description"
            android:value="Notifications for Flic2 button connections" />
        <meta-data
            android:name="nl.xguard.flic2.notification_id"
            android:value="123321" />
        <meta-data
            android:name="nl.xguard.flic2.notification_channel_id"
            android:value="my_custom_channel_id" />
    </application>
</manifest>
```

**Available Configuration Options:**

- `nl.xguard.flic2.notification_title` - Notification title (default: "Flic 2")
- `nl.xguard.flic2.notification_text` - Notification text (default: "Flic 2 service is running")
- `nl.xguard.flic2.notification_icon` - Notification icon resource ID (default: system info icon)
  - Use `@drawable/your_icon_name` or `@mipmap/your_icon_name` format
- `nl.xguard.flic2.notification_channel_name` - Notification channel name (default: "Flic2Channel")
- `nl.xguard.flic2.notification_channel_description` - Notification channel description (default: "Flic2Channel")
- `nl.xguard.flic2.notification_id` - Notification ID integer (default: 123321)
- `nl.xguard.flic2.notification_channel_id` - Notification channel ID string (default: "Notification_Channel_Flic2Service")

**Example with Custom Icon:**

1. Add your notification icon to `android/app/src/main/res/drawable/` (e.g., `ic_flic2_notification.png`)

2. Add metadata to `AndroidManifest.xml`:
```xml
<meta-data
    android:name="nl.xguard.flic2.notification_icon"
    android:resource="@drawable/ic_flic2_notification" />
```

**Note:** The notification icon must be a white/transparent icon suitable for Android notifications. If you don't specify a custom icon, the system default info icon will be used.

## Basic Usage

### 1. Initialize the Library (Global Setup)

**Important:** For background usage, initialize Flic2 at the global level (outside of React components), typically in your app's entry point (e.g., `index.js` or `App.js`). Initializing in a `useEffect` is too late for background functionality.

```tsx
// index.js or App.js (global level, outside components)
import Flic2, {
  ButtonEvent,
  ManagerStateChangeEvent,
} from 'react-native-flic2';

// Initialize the Flic2 manager when app starts
(async () => {
  try {
    await Flic2.initialize();
    console.log('Flic2 initialized');

    // Connect to all previously known buttons
    Flic2.connectAllKnownButtons();

    // Set up global event listeners for background usage
    Flic2.eventEmitter.on('buttonEvent', (event: ButtonEvent) => {
      console.log('Button event:', event.event, event.button.name);

      // Handle button events that need to work in background
      if (event.event === 'click') {
        // Your background logic here (e.g., send notification, update database)
      }
    });

    Flic2.eventEmitter.on('managerStateChange', (event: ManagerStateChangeEvent) => {
      console.log('Manager state:', event.stateName);
    });
  } catch (error) {
    console.error('Failed to initialize Flic2:', error);
  }
})();
```

### 2. Set Up Component-Level Event Listeners (Optional)

If you need to update UI based on button events, you can add additional listeners in your components using `useEffect`. These listeners are in addition to the global ones and are useful for UI-specific updates:

```tsx
import React, { useEffect } from 'react';
import { Alert } from 'react-native';
import Flic2, {
  ButtonEvent,
  ScanStatusChangeEvent,
} from 'react-native-flic2';

const MyComponent = () => {
  useEffect(() => {
    // UI-specific listener (only needed if you want to show UI updates)
    const buttonSubscription = Flic2.eventEmitter.on(
      'buttonEvent',
      (event: ButtonEvent) => {
        if (event.event === 'click' || event.event === 'doubleClick' || event.event === 'hold') {
          // Show UI alert when component is mounted
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
        // Update UI based on scan status
        if (event.event === 'started') {
          console.log('Scan started');
        } else if (event.event === 'completion') {
          console.log('Scan completed');
        }
      }
    );

    // Cleanup subscriptions on unmount
    return () => {
      buttonSubscription.remove();
      scanSubscription.remove();
    };
  }, []);

  // ... rest of component
};
```

**Note:** Global listeners (set up outside components) will continue to work even when components unmount, which is essential for background functionality. Component-level listeners are only active when the component is mounted.

### 3. Complete Example

This example shows a component that manages the UI for Flic2 buttons. **Note:** Flic2 should be initialized globally (see section 1) before this component is used. This component only handles UI-specific functionality.

**Important:** This is just an example demonstrating the library's API. Some parts (like `Alert.prompt` used in `renameButton`) are iOS-only and need platform-specific implementations for Android. Adapt the UI components to your needs and platform requirements.

```tsx
import React, { useState, useEffect } from 'react';
import { View, Text, Button, Alert, StyleSheet } from 'react-native';
import Flic2, {
  ButtonEvent,
  FlicButton,
  ScanStatusChangeEvent,
} from 'react-native-flic2';

const Flic2Example = () => {
  const [buttons, setButtons] = useState<FlicButton[]>([]);
  const [isScanning, setIsScanning] = useState(false);

  useEffect(() => {
    // Load existing buttons when component mounts
    // (Flic2 should already be initialized globally)
    loadButtons();

    // Set up UI-specific event listeners
    // Note: Global listeners should be set up outside components for background usage
    const buttonSubscription = Flic2.eventEmitter.on(
      'buttonEvent',
      (event: ButtonEvent) => {
        console.log('Button event:', event.event, event.button.name);

        // Show UI alerts when component is mounted
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

        // Update UI state based on scan status
        if (event.event === 'started') {
          setIsScanning(true);
        } else if (event.event === 'completion') {
          setIsScanning(false);
          loadButtons(); // Refresh button list after scan
        }
      }
    );

    // Cleanup subscriptions on unmount
    return () => {
      buttonSubscription.remove();
      scanSubscription.remove();
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
    // Note: Alert.prompt is iOS-only. On Android, use a TextInput in a Modal
    // or a library like react-native-prompt-android for cross-platform support.
    // This is just an example - implement appropriately for your platform needs.
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

#### `initialize(): Promise<void>`

Initialize the Flic2 manager. This must be called before using any other methods.

```tsx
await Flic2.initialize();
```

### Scanning

#### `startScan(): Promise<void>`

Start scanning for new Flic2 buttons. The scan will emit `scanStatusChange` events.

```tsx
await Flic2.startScan();
```

#### `stopScan(): Promise<void>`

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

#### `connectAllKnownButtons(): Promise<void>`

Connect to all previously known buttons.

```tsx
await Flic2.connectAllKnownButtons();
```

#### `disconnectAllKnownButtons(): Promise<void>`

Disconnect all connected buttons.

```tsx
await Flic2.disconnectAllKnownButtons();
```

#### `forgetButton(uuid: string): Promise<void>`

Forget (unpair) a specific button.

```tsx
await Flic2.forgetButton('button-uuid');
```

#### `forgetAllButtons(): Promise<void>`

Forget all buttons.

```tsx
await Flic2.forgetAllButtons();
```

### Button Configuration

#### `buttonConnect(uuid: string): Promise<FlicButton>`

Connect to a specific button. Returns the button object.

```tsx
const button = await Flic2.buttonConnect('button-uuid');
```

#### `buttonDisconnect(uuid: string): Promise<FlicButton>`

Disconnect a specific button. Returns the button object.

```tsx
const button = await Flic2.buttonDisconnect('button-uuid');
```

#### `buttonSetNickname(uuid: string, nickname: string): Promise<FlicButton>`

Set a custom nickname for a button. Returns the updated button object.

```tsx
const button = await Flic2.buttonSetNickname('button-uuid', 'My Button');
```

#### `buttonSetTriggerMode(uuid: string, mode: TriggerModeType): Promise<FlicButton>`

Set the trigger mode for a button. Returns the updated button object. Modes:
- `0`: Click and Hold
- `1`: Click and Double Click
- `2`: Click and Double Click and Hold
- `3`: Click only

**Note:** This method is only supported on iOS. On Android, it will reject with an error.

```tsx
const button = await Flic2.buttonSetTriggerMode('button-uuid', 3); // Click only
```

#### `buttonSetLatencyMode(uuid: string, mode: LatencyModeType): Promise<FlicButton>`

Set the latency mode for a button. Returns the updated button object. Modes:
- `0`: Normal latency
- `1`: Low latency

**Note:** This method is only supported on iOS. On Android, it will reject with an error.

```tsx
const button = await Flic2.buttonSetLatencyMode('button-uuid', 1); // Low latency
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

Initialize Flic2 globally when your app starts (not in a component):

```tsx
// index.js or App.js (global level)
import Flic2 from 'react-native-flic2';

(async () => {
  await Flic2.initialize();
  Flic2.connectAllKnownButtons();
})();
```

### Handling Button Clicks

Set up button event listeners globally for background usage:

```tsx
// Global level (e.g., index.js or App.js)
Flic2.eventEmitter.on('buttonEvent', (event) => {
  if (event.event === 'click') {
    // Handle single click (works in background)
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

### Running on iOS Simulator

`flic2lib` is device-only. For simulator builds, use the no-op simulator pod and a
dedicated simulator build configuration.

1. Add a simulator configuration in Xcode (for example `DebugSimulator`) and a scheme that uses it.
2. Map pods by configuration in your app `Podfile`:

```ruby
# Example
project 'YourApp.xcodeproj', {
  'Debug' => :debug,
  'DebugSimulator' => :debug,
  'Release' => :release,
}

target 'YourApp' do
  # ... your existing use_react_native! setup

  pod 'Flic2Device',
    :path => '../node_modules/react-native-flic2',
    :configurations => ['Debug', 'Release']

  pod 'Flic2Simulator',
    :path => '../node_modules/react-native-flic2',
    :configurations => ['DebugSimulator']
end
```

3. Install pods normally (no env flags):

```sh
cd ios && bundle exec pod install && cd ..
```

4. Launch simulator build (example):

```sh
npx react-native run-ios --scheme YourAppSimulator --mode DebugSimulator --simulator "iPhone 17"
```

Expected behavior on simulator:
- App builds and launches.
- Flic calls are no-op fallback behavior.
- Real button communication works only on physical devices.

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

- Make sure you've called `initialize()` globally (outside components) before setting up event listeners
- For background usage, set up listeners at the global level, not in `useEffect`
- Verify the button is connected and ready (`button.isReady === true`)
- Check that the button's trigger mode supports the event you're listening for

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

See [LICENSE](LICENSE)
