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
import java.util.List;
import java.util.Objects;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import nl.xguard.flic2.callback.ReactFlic2ScanCallback;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.model.ReactAndroidHandler;
import nl.xguard.flic2.model.ReactFlic2Button;
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
    private ArrayList<ReactFlic2Button> mReactFlic2Buttons = new ArrayList<>();

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

        for (Flic2Button button : mFlic2Manager.getButtons()) {
            registerFlic2Button(button);
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
        Log.d(TAG, "registerFlic2Button() called with: flic2Button = [" + flic2Button + "]");
        final ReactFlic2Button reactFlic2Button = new ReactFlic2Button(flic2Button);
        mReactFlic2Buttons.add(reactFlic2Button);
    }

    @ReactMethod
    public void connectAllKnownButtons() {
        Log.d(TAG, "connectAllKnownButtons()");
        for (Flic2Button button : mFlic2Manager.getButtons()) {
            button.connect();
        }
    }

    @ReactMethod
    public void connectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "connectButton()");
        List<Flic2Button> buttons = mFlic2Manager.getButtons();
        for (Flic2Button button : buttons) {
            Log.d(TAG, "connectButton() uuid: " + uuid + " " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid())) {
                button.connect();
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "connectButton() no button found " + uuid);
    }

    @ReactMethod
    public void disconnectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "disconnectButton()");
        List<Flic2Button> buttons = mFlic2Manager.getButtons();
        for (Flic2Button button : buttons) {
            Log.d(TAG, "disconnectButton() uuid: " + uuid + " " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid())) {
                button.disconnectOrAbortPendingConnection();
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "disconnectButton() no button found " + uuid);
    }

    @ReactMethod
    public void disconnectAllKnownButtons() {
        Log.d(TAG, "disconnectAllKnownButtons()");
        for (Flic2Button button : mFlic2Manager.getButtons()) {
            button.disconnectOrAbortPendingConnection();
        }
    }

    @ReactMethod
    public void setName(String uuid, String name, Callback successCallback) {
        Log.d(TAG, "setName()");
        List<Flic2Button> buttons = mFlic2Manager.getButtons();
        for (Flic2Button button : buttons) {
            Log.d(TAG, "setName() uuid: " + uuid + " " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid())) {
                button.setName(name);
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "setName() no button found " + uuid);
    }

    @ReactMethod
    public void stopScan() {
        Log.d(TAG, "stopScanning()");
        mFlic2Manager.stopScan();
    }

    @ReactMethod
    public void getButtons(Callback successCallback, Callback errorCallback) {
        Log.d(TAG, "getButtons()");
        try {
            List<Flic2Button> buttons = mFlic2Manager.getButtons();
            WritableArray array = new WritableNativeArray();
            for (Flic2Button button : buttons) {
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
        Log.d(TAG, "forgetButton() called with: uuid = [" + uuid + "], successCallback = [" + successCallback + "]");
        List<Flic2Button> buttons = mFlic2Manager.getButtons();
        for (Flic2Button button : buttons) {
            Log.d(TAG, "forgetButton() uuid: " + uuid + " " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid())) {
                button.disconnectOrAbortPendingConnection();
                mFlic2Manager.forgetButton(button);
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "forgetButton() no button found " + uuid);
    }

    @ReactMethod
    public void forgetAllButtons() {
        Log.d(TAG, "forgetAllButtons()");
        List<Flic2Button> buttons = mFlic2Manager.getButtons();
        for (Flic2Button button : buttons) {
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