package nl.xguard.flic2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;

import java.util.Objects;

import io.flic.flic2libandroid.Flic2Manager;
import nl.xguard.flic2.model.ReactFlic2ButtonListener;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.model.ReactAndroidHandler;
import nl.xguard.flic2.model.ReactFlic2Manager;
import nl.xguard.flic2.model.ReactLogger;
import nl.xguard.flic2.service.Flic2Service;
import nl.xguard.flic2.util.SisAction;


import static nl.xguard.flic2.util.ActivityUtil.isServiceRunning;
import static nl.xguard.flic2.util.ActivityUtil.startForegroundService;

public class Flic2 extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = Flic2.class.getSimpleName();

    private ReactFlic2Manager mReactFlic2Manager;

    @NonNull
    @Override
    public String getName() {
        return Flic2.class.getSimpleName();
    }

    @ReactMethod
    public Flic2(ReactApplicationContext reactContext) {
        super(reactContext);

        getReactApplicationContext().addLifecycleEventListener(this);
        ReactEvent.createInstance(reactContext);
    }

    @ReactMethod
    public void startup() {
        try {
            Flic2Manager.init(getReactApplicationContext(), new ReactAndroidHandler(new Handler()), new ReactLogger());
            mReactFlic2Manager = new ReactFlic2Manager(Flic2Manager.getInstance(), new ReactFlic2ButtonListener());
        } catch (Exception e) {
            Log.e(TAG, "Flic2: ", e);
        }

        mReactFlic2Manager.initButtons();

    }

    @Deprecated
    public static void startupAndroid(Context context, Handler handler) {
        Log.w(TAG, "startupAndroid() is deprecated and no longer used, use startup() init and get a Flic2Manager");
    }

    @ReactMethod
    public void startService() {
        Log.d(TAG, "startService()");

        Context context = getReactApplicationContext();
        boolean isRunning = isServiceRunning(context, Flic2Service.class);
        if (!isRunning) {
            Intent intent = new Intent(context, Flic2Service.class);
            startForegroundService(context, intent);
        }
    }

    @ReactMethod
    public void connectAllKnownButtons() {
        Log.d(TAG, "connectAllKnownButtons()");
        mReactFlic2Manager.connectAllButtons();
    }

    @ReactMethod
    public void connectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "connectButton() called with: uuid = [" + uuid + "], successCallback = []");

        mReactFlic2Manager.connectButton(uuid);
        successCallback.invoke();
    }

    @ReactMethod
    public void disconnectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "disconnectButton()");

        mReactFlic2Manager.disconnectButton(uuid);
        successCallback.invoke();
    }

    @ReactMethod
    public void disconnectAllKnownButtons() {
        Log.d(TAG, "disconnectAllKnownButtons() called");
        mReactFlic2Manager.disconnectAllKnownButtons();
    }

    @ReactMethod
    public void setName(String uuid, String name, Callback successCallback) {
        Log.d(TAG, "setName() called with: uuid = [" + uuid + "], name = [" + name + "], successCallback = []");

        mReactFlic2Manager.setName(uuid, name);
        successCallback.invoke();
    }

    @ReactMethod
    public void getButtons(Callback successCallback, Callback errorCallback) {
        Log.d(TAG, "getButtons() called with: successCallback = [], errorCallback = []");
        try {
            WritableArray array = mReactFlic2Manager.getButtons();
            successCallback.invoke(array);
        } catch (Exception e) {
            errorCallback.invoke("Error getting buttons", e.getMessage());
        }
    }

    @ReactMethod
    public void forgetButton(String uuid, Callback successCallback) {
        Log.d(TAG, "forgetButton() called with: uuid = [" + uuid + "], successCallback = []");

        mReactFlic2Manager.forgetButton(uuid);
        successCallback.invoke();
    }

    @ReactMethod
    public void forgetAllButtons() {
        Log.d(TAG, "forgetAllButtons() called");
        mReactFlic2Manager.forgetAllButtons();
    }


    @ReactMethod
    public void startScan() {
        Log.d(TAG, "startScan() called");

        mReactFlic2Manager.startScan();
    }

    @ReactMethod
    public void stopScan() {
        Log.d(TAG, "stopScan() called");
        mReactFlic2Manager.stopScan();
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
        // empty
    }

    @Override
    public void onHostDestroy() {
        // empty
    }
}