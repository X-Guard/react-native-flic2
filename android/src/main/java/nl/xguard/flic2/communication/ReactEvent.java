package nl.xguard.flic2.communication;

import android.util.Log;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import io.flic.flic2libandroid.Flic2Button;

public class ReactEvent {
    private static final String TAG = "Flic2 event:";
    private static final String EVENT_NAMESPACE = "FLIC2";
    private static final String KEY_EVENT = "event";
    private static final String KEY_VALUE = "value";
    private static final String KEY_BUTTON_ID = "uuid";

    public static final String EVENT_MANAGER_INITIALIZED = "initFlic2";
    public static final String EVENT_MANAGER_IS_INITIALIZED = "isInitialized";

    public static final String EVENT_BUTTON_STATUS_DISCONNECTED = "BUTTON_DISCONNECTED";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_ON_READY = "BUTTON_CONNECTION_READY";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_STARTED = "BUTTON_CONNECTION_STARTED";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_COMPLETED = "BUTTON_CONNECTION_COMPLETED";
    public static final String EVENT_BUTTON_STATUS_CONNECTION_UNPAIRED = "BUTTON_CONNECTION_UNPAIRED";
    public static final String EVENT_BUTTON_STATUS_UNKNOWN = "BUTTON_CONNECTION_UNKNOWN";
    public static final String EVENT_BUTTON_STATUS_ON_FAILURE = "BUTTON_FAILURE";
    public static final String EVENT_BUTTON_GRABBED = "didGrabFlicButton";
    public static final String EVENT_NO_BUTTON_GRABBED = "didGrabFlicButtonError";
    public static final String EVENT_BUTTON_DOWN = "didReceiveButtonDown";
    public static final String EVENT_BUTTON_UP = "didReceiveButtonUp";
    public static final String EVENT_BUTTON_SINGLE_CLICK = "didReceiveButtonClick";
    public static final String EVENT_BUTTON_DOUBLE_CLICK = "didReceiveButtonDoubleClick";
    public static final String EVENT_BUTTON_HOLD = "didReceiveButtonHold";
    public static final String EVENT_BUTTON_REMOVED = "removeAllButtons";
    public static final String EVENT_BUTTON_BATTERY_LEVEL = "batteryLevel";


    private ReactContext mReactContext;

    public ReactEvent(ReactContext reactContext) {
        mReactContext = reactContext;
    }


    public void send(Flic2Button button, String event) {
        Log.d(TAG, "sendEventMessage() called with: button = [" + button + "], event = [" + event + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);
        args.putString(KEY_BUTTON_ID, button.getUuid());

        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_NAMESPACE, args);
    }

    public void send(Flic2Button button, String event, Boolean queued, long age) {
        Log.d(TAG, "sendEventMessage() called with: button = [" + button + "], event = [" + event + "]");
        WritableMap args = new WritableNativeMap();
        args.putString(KEY_EVENT, event);
        args.putString(KEY_BUTTON_ID, button.getUuid());
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

}