package nl.xguard.flic2.communication;

import android.util.Log;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import io.flic.flic2libandroid.Flic2Button;

public class ReactEvent {
    private static final String TAG = "Flic2ReactEvent";
    private static final String EVENT_NAMESPACE = "didReceiveButtonEvent";
    private static final String KEY_EVENT = "event";
    private static final String KEY_VALUE = "value";
    private static final String KEY_BUTTON_ID = "uuid";
    private static final String KEY_BUTTON_SERIAL = "serial";
    private static final String KEY_BUTTON_NAME = "name";
    private static final String KEY_BUTTON_BatteryLevel = "batteryLevel";
    private static final String KEY_BUTTON_Voltage = "voltage";
    private static final String KEY_BUTTON_ISREADY = "isReady";
    private static final String KEY_BUTTON_ISUNPAIRED = "isUnpaired";
    private static final String KEY_BUTTON_PRESSCOUNT = "pressCount";
    private static final String KEY_BUTTON_FIRMWAREREVISION = "firmwareRevision";
    private static final String KEY_BUTTON_BLUETOOTHADDRESS = "bluetoothAddress";
    private static final String KEY_BUTTON_READY_TIME = "isReadyTimeStamp";

    public static final String EVENT_MANAGER_INITIALIZED = "initFlic2";
    public static final String EVENT_MANAGER_IS_INITIALIZED = "isInitialized";

    public static final String EVENT_BUTTON_STATUS_DISCONNECTED = "buttonDisconnected";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_ON_READY = "buttonConnectionReady";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_STARTED = "buttonConnectionStarted";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_COMPLETED = "buttonConnectionCompleted";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_UNPAIRED = "buttonConnectionUnpaired";
    public static final String EVENT_BUTTON_STATUS_UNKNOWN = "buttonConnectionUnknown";
    public static final String EVENT_BUTTON_STATUS_ON_FAILURE = "buttonConnectionFailure";
    public static final String EVENT_BUTTON_DOWN = "didReceiveButtonDown";
    public static final String EVENT_BUTTON_UP = "didReceiveButtonUp";
    public static final String EVENT_BUTTON_SINGLE_CLICK = "didReceiveButtonClick";
    public static final String EVENT_BUTTON_DOUBLE_CLICK = "didReceiveButtonDoubleClick";
    public static final String EVENT_BUTTON_HOLD = "didReceiveButtonHold";
    public static final String EVENT_BUTTON_REMOVED = "removeAllButtons";
    public static final String EVENT_BUTTON_BATTERY_LEVEL = "batteryLevel";
    public static final String EVENT_BUTTON_NEW_NAME = "didReceiveNewName";
    public static final String EVENT_BUTTON_NEW_FIRMWARE = "didReceiveNewFirmware";
    public static final String EVENT_SCAN_RESULT = "scanResult";


    private ReactContext mReactContext;

    public ReactEvent(ReactContext reactContext) {
        mReactContext = reactContext;
    }


    public void send(Flic2Button button, String event) {
        Log.d(TAG, "sendEventMessage() called with: button = [" + button + "], event = [" + event + "]");
        WritableMap args = new WritableNativeMap();
        WritableMap buttonMap = this.getButtonArgs(button);
        args.putString(KEY_EVENT, event);
        args.putMap("button", buttonMap);


        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    public void send(Flic2Button button, String event, Boolean queued, long age) {
        Log.d(TAG, "sendEventMessage() called with: button = [" + button + "], event = [" + event + "]");
        WritableMap args = new WritableNativeMap();
        WritableMap buttonMap = this.getButtonArgs(button);
        args.putString(KEY_EVENT, event);
        args.putMap("button", buttonMap);
        args.putBoolean("queued", queued);
        args.putString("age", String.valueOf(age));

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    public void send(String event) {
        Log.d(TAG, "sendEventMessage() called with: event = [" + event + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    private void send(String event, String value, String keyValue) {
        Log.d(TAG, "sendEventMessage() called with an string: event = [" + event + "], value = [" + value + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);
        args.putString(keyValue, value);

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    public void send(String event, int value, String keyValue) {
        Log.d(TAG, "sendEventMessage() called with an int: event = [" + event + "], value = [" + value + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);
        args.putInt(keyValue, value);

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    public void send(String event, Boolean value, String keyValue) {
        Log.d(TAG, "sendEventMessage() called with an boolean: event = [" + event + "], value = [" + value + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);
        args.putBoolean(keyValue, value);

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    public void send(String event, int level) {
        Log.d(TAG, "sendEventMessage() called with: event = [" + event + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);
        args.putInt("BatteryLevel", level);

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }


    public void sendScanMessage(Boolean error,int code, Flic2Button button) {
        Log.d(TAG, "sendScanMessage() called with: event = [" + EVENT_SCAN_RESULT + "]");
        WritableMap args = new WritableNativeMap();
        WritableMap buttonMap = this.getButtonArgs(button);
        args.putBoolean("error", error);
        args.putInt("result", code);
        args.putMap("button", buttonMap);

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_SCAN_RESULT, args);
    }

    public WritableMap getButtonArgs(Flic2Button button) {
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_BUTTON_ID, button.getUuid());
        args.putString(KEY_BUTTON_SERIAL, button.getSerialNumber());
        args.putString(KEY_BUTTON_BLUETOOTHADDRESS, button.getBdAddr());
        args.putString(KEY_BUTTON_NAME, button.getName());
        args.putBoolean(KEY_BUTTON_ISUNPAIRED, button.isUnpaired());
        args.putInt(KEY_BUTTON_PRESSCOUNT, button.getPressCount());
        args.putInt(KEY_BUTTON_FIRMWAREREVISION, button.getFirmwareVersion());
        args.putString(KEY_BUTTON_READY_TIME, String.valueOf(button.getReadyTimestamp()));

        if (button.getConnectionState() == Flic2Button.CONNECTION_STATE_CONNECTED_READY) {
          args.putBoolean(KEY_BUTTON_ISREADY, true);
        } else {
          args.putBoolean(KEY_BUTTON_ISREADY, false);
        }

        if (button.getLastKnownBatteryLevel() != null) {
            args.putInt(KEY_BUTTON_BatteryLevel, button.getLastKnownBatteryLevel().getEstimatedPercentage());
            args.putDouble(KEY_BUTTON_Voltage, button.getLastKnownBatteryLevel().getVoltage());
        }

        return args;
    }

}