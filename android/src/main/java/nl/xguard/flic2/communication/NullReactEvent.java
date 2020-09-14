package nl.xguard.flic2.communication;

import android.util.Log;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import io.flic.flic2libandroid.Flic2Button;

public class NullReactEvent implements IReactEvent {
    private static final String TAG = "NullReactEvent";

    public NullReactEvent() {
    }

    @Override
    public void send(String event, String value) {
        Log.d(TAG, "sendEventMessage() called with: event = [" + event + "], value = [" + value + "]");
    }

    @Override
    public void send(Flic2Button flic2Button, String value) {
        Log.d(TAG, "send() called with: button = [" + flic2Button + "], value = [" + value + "]");
    }

    @Override
    public void send(Flic2Button button, String event, Boolean queued, long age) {
        Log.d(TAG, "send() called with: button = [" + button + "], event = [" + event + "], queued = [" + queued + "], age = [" + age + "]");
    }

    @Override
    public WritableMap getButtonArgs(Flic2Button button) {
        WritableMap args = new WritableNativeMap();
        args.putString(TAG, "Called from NullReactEvent");
        return args;
    }

    @Override
    public void sendScanStatusMessage(String event) {
        Log.d(TAG, "sendScanStatusMessage() called with: event = [" + event + "]");
    }

    @Override
    public void sendScanMessage(String event, Boolean error, int code, Flic2Button button) {
        Log.d(TAG, "sendScanMessage() called with: event = [" + event + "], error = [" + error + "], code = [" + code + "], button = [" + button + "]");
    }
}
