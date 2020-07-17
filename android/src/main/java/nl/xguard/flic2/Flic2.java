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
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.ArrayList;
import java.util.Objects;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import nl.xguard.flic2.callback.ReactFlic2ButtonListener;
import nl.xguard.flic2.callback.ReactFlic2ScanCallback;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.model.ReactAndroidHandler;
import nl.xguard.flic2.model.ReactLogger;
import nl.xguard.flic2.service.Flic2Service;
import nl.xguard.flic2.util.SisAction;


import static nl.xguard.flic2.util.ActivityUtil.isServiceRunning;
import static nl.xguard.flic2.util.ActivityUtil.startForegroundService;

public class Flic2 extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = Flic2.class.getSimpleName();

    private SisAction mAction = createDefaultAction();
    private static final Integer PERMISSION_REQUEST_CODE = 741;

    private Flic2Manager mFlic2Manager = null;
    private ReactFlic2ButtonListener mReactFlic2ButtonListener = new ReactFlic2ButtonListener();
    private ArrayList<Flic2Button> mRegisteredFlic2Buttons = new ArrayList<>();

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
            mFlic2Manager = Flic2Manager.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Flic2: ", e);
        }

        for (Flic2Button flic2Button : mFlic2Manager.getButtons()) {
            registerFlic2Button(flic2Button);
        }

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

    private void registerFlic2Button(Flic2Button flic2Button) {
        flic2Button.addListener(mReactFlic2ButtonListener);
        mRegisteredFlic2Buttons.add(flic2Button);
    }

    private void unregisterFlic2Button(Flic2Button flic2Button) {
        mRegisteredFlic2Buttons.remove(flic2Button);
        flic2Button.removeListener(mReactFlic2ButtonListener);
    }

    @ReactMethod
    public void connectAllKnownButtons() {
        Log.d(TAG, "connectAllKnownButtons()");
        for (Flic2Button button : mRegisteredFlic2Buttons) {
            button.connect();
        }
    }

    private Flic2Button getButton(String uuid) {
        Flic2Button flic2Button = getButtonByBdAddr(uuid);
        if (flic2Button != null) {
            Log.d(TAG, "getButton() found with uuid: " + flic2Button.getUuid());
        } else {
            Log.d(TAG, "getButton() no button found " + uuid);
        }
        return flic2Button;
    }

    private Flic2Button getButtonByBdAddr(String uuid) {
        for (Flic2Button button : mRegisteredFlic2Buttons) {
            if (button.getUuid().equalsIgnoreCase(uuid)) {
                return button;
            }
        }
        return null;
    }

    @ReactMethod
    public void connectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "connectButton() called with: uuid = [" + uuid + "], successCallback = []");

        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            flic2Button.connect();
            registerFlic2Button(flic2Button);
        }
        successCallback.invoke();
    }

    @ReactMethod
    public void disconnectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "disconnectButton()");

        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            unregisterFlic2Button(flic2Button);
            flic2Button.disconnectOrAbortPendingConnection();
        }

        successCallback.invoke();
    }

    @ReactMethod
    public void disconnectAllKnownButtons() {
        Log.d(TAG, "disconnectAllKnownButtons() called");
        for (Flic2Button button : mRegisteredFlic2Buttons) {
            unregisterFlic2Button(button);
            button.disconnectOrAbortPendingConnection();
        }
    }

    @ReactMethod
    public void setName(String uuid, String name, Callback successCallback) {
        Log.d(TAG, "setName() called with: uuid = [" + uuid + "], name = [" + name + "], successCallback = []");

        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            flic2Button.setName(name);
        }

        successCallback.invoke();
    }

    @ReactMethod
    public void getButtons(Callback successCallback, Callback errorCallback) {
        Log.d(TAG, "getButtons() called with: successCallback = [], errorCallback = []");
        try {
            WritableArray array = new WritableNativeArray();
            for (Flic2Button button : mRegisteredFlic2Buttons) {
                WritableMap map = ReactEvent.getInstance().getButtonArgs(button);
                array.pushMap(map);
            }
            successCallback.invoke(array);
        } catch (Exception e) {
            errorCallback.invoke("Error getting buttons", e.getMessage());
        }
    }

    @ReactMethod
    public void forgetButton(String uuid, Callback successCallback) {
        Log.d(TAG, "forgetButton() called with: uuid = [" + uuid + "], successCallback = []");

        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            unregisterFlic2Button(flic2Button);
            flic2Button.disconnectOrAbortPendingConnection();
            mFlic2Manager.forgetButton(flic2Button);
        }

        successCallback.invoke();
    }

    @ReactMethod
    public void forgetAllButtons() {
        Log.d(TAG, "forgetAllButtons() called");

        for (Flic2Button button : mRegisteredFlic2Buttons) {
            unregisterFlic2Button(button);
            button.disconnectOrAbortPendingConnection();
            mFlic2Manager.forgetButton(button);
        }
    }

    @ReactMethod
    public void startScan() {
        Log.d(TAG, "startScan() called");
        if (isPermissionDenied()) {
            requestPermission();
            setResumeAction(this::startScan);
            return;
        }

        mFlic2Manager.startScan(new ReactFlic2ScanCallback(this::registerFlic2Button));
    }

    @ReactMethod
    public void stopScan() {
        Log.d(TAG, "stopScan() called");
        mFlic2Manager.stopScan();
    }

    private boolean isPermissionDenied() {
        int permissionCheck = ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_DENIED;
    }

    private void requestPermission() {
        Log.d(TAG, "requestPermission() called");
        if (isPermissionDenied()) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getCurrentActivity()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    private void setResumeAction(SisAction action) {
        mAction = () -> {
            action.call();
            mAction = createDefaultAction();
        };
    }

    private SisAction createDefaultAction() {
        return () -> {
        };
    }

    @Override
    public void onHostResume() {
        mAction.call();
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