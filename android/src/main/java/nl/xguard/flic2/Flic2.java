package nl.xguard.flic2;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import io.flic.flic2libandroid.Flic2ButtonListener;
import nl.xguard.flic2.callback.flic2ButtonCallback;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.service.Flic2Service;

import java.util.ArrayList;
import java.util.List;
import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import io.flic.flic2libandroid.HandlerInterface;
import io.flic.flic2libandroid.LoggerInterface;
import nl.xguard.flic2.sharedpreferences.flic2SharedPreferences;

public class Flic2 extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "Flic2Module";
    private ReactApplicationContext mreactContext;
    private Flic2Manager manager;
    private ReactEvent mReactEvent;
    private String packageName;
    private Handler handler;
    private boolean isScanning;
    private boolean managerIsReady;
    private ReactInstanceManager reactInstanceManager;
    private flic2SharedPreferences sisSharedPreferences;

    ArrayList<ButtonData> dataSet = new ArrayList<>();

    @Override
    public String getName() {
        return Flic2.class.getSimpleName();
    }

    @ReactMethod
    public Flic2(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d(TAG, "onCreate()");
        managerIsReady = false;
        mreactContext = reactContext;
        mReactEvent = new ReactEvent(mreactContext);
        handler = new Handler(mreactContext.getMainLooper());
        packageName = mreactContext.getPackageName();
        reactContext.addLifecycleEventListener(this);
        ReactApplication reactApplication = (ReactApplication)reactContext.getApplicationContext().getApplicationContext();
        reactInstanceManager = reactApplication.getReactNativeHost().getReactInstanceManager();
        sisSharedPreferences = flic2SharedPreferences.getInstance(reactContext);


        sisSharedPreferences.write(flic2SharedPreferences.PREF_KEY_IS_RUNNING, true);
    }

    @Override
    public void onHostResume() {
        // Activity `onResume`
        Log.d(TAG, "onHostResume()");
    }

    @Override
    public void onHostPause() {
        // Activity `onPause`
        Log.d(TAG, "onHostPause()");
    }

    @Override
    public void onHostDestroy() {
        // Activity `onDestroy`
        Log.d(TAG, "onHostDestroy()");



        sisSharedPreferences.write(flic2SharedPreferences.PREF_KEY_IS_RUNNING, false);
//        for (ButtonData data : dataSet) {
//            data.button.removeListener(data.listener);
//        }
    }

    @ReactMethod
    public void startup() {
        Log.d(TAG, "startup()");
        manager = Flic2Manager.initAndGetInstance(mreactContext, handler);
        managerIsReady = true;
        mReactEvent.sendEvent("managerInitialized");
    }

    @ReactMethod
    @TargetApi(23)
    public void startService() {
        Log.d(TAG, "startService()");
        Boolean isRunning = isServiceRunning(mreactContext, Flic2Service.class);
        Log.d(TAG, "startService()" + !isRunning + " " + isRunning);
        if (!isRunning) {
            Intent intent = new Intent(mreactContext, Flic2Service.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "startService() new");
                mreactContext.startForegroundService(intent);
            } else {
                Log.d(TAG, "startService() old" );
                mreactContext.startService(intent);
            }

            for (Flic2Button button : manager.getButtons()) {
                button.connect();
                listenToButton(button);
            }
        } else {
            Log.d(TAG, "startService(): service is already running");
        }
    }

    public void listenToButton(Flic2Button button) {
        Log.d(TAG, "listenToButton()");
        final ButtonData buttonData = new ButtonData(button);
        buttonData.listener = new flic2ButtonCallback(mreactContext, reactInstanceManager, packageName);
        button.addListener(buttonData.listener);
        dataSet.add(buttonData);
    }

    @ReactMethod
    @TargetApi(23)
    public void connectAllKnownButtons() {
        Log.d(TAG, "connectAllKnownButtons()");
        for (Flic2Button button : manager.getButtons()) {
//            button.disconnectOrAbortPendingConnection();
            button.connect();
//            listenToButton(button);
        }
    }

    @ReactMethod
    @TargetApi(23)
    public void connectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "connectButton()");
        List<Flic2Button> buttons = manager.getButtons();
        for (Flic2Button button: buttons) {
            Log.d(TAG, "connectButton() uuid: " + uuid +" " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid()))
            {
//        button.disconnectOrAbortPendingConnection();
                button.connect();
                // listenToButton(button);
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "connectButton() no button found " + uuid);
    }
    @ReactMethod
    @TargetApi(23)
    public void disconnectButton(String uuid, Callback successCallback) {
        Log.d(TAG, "disconnectButton()");
        List<Flic2Button> buttons = manager.getButtons();
        for (Flic2Button button: buttons) {
            Log.d(TAG, "disconnectButton() uuid: " + uuid +" " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid()))
            {
                button.disconnectOrAbortPendingConnection();
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "disconnectButton() no button found " + uuid);
    }
    @ReactMethod
    @TargetApi(23)
    public void disconnectAllKnownButtons() {
        Log.d(TAG, "disconnectAllKnownButtons()");
        for (Flic2Button button : manager.getButtons()) {
            button.disconnectOrAbortPendingConnection();
        }
    }
    @ReactMethod
    @TargetApi(23)
    public void setName(String uuid, String name, Callback successCallback) {
        Log.d(TAG, "setName()");
        List<Flic2Button> buttons = manager.getButtons();
        for (Flic2Button button: buttons) {
            Log.d(TAG, "setName() uuid: " + uuid +" " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid()))
            {
                button.setName(name);
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "setName() no button found " + uuid);
    }
    @ReactMethod
    @TargetApi(23)
    public void stopScan() {
        Log.d(TAG, "stopScanning()");
        manager.stopScan();
        isScanning = false;
    }
    @ReactMethod
    @TargetApi(23)
    public void getButtons(Callback successCallback, Callback errorCallback) {
        Log.d(TAG, "getButtons()");
        try {
            List<Flic2Button> buttons = manager.getButtons();
            WritableArray array = new WritableNativeArray();
            for (Flic2Button button: buttons) {
                WritableMap map = mReactEvent.getButtonArgs(button);
                array.pushMap(map);
            }
            successCallback.invoke(array);
        }  catch (Exception e) {
            errorCallback.invoke("Error getting buttons", e.getMessage());
        }
    }
    @ReactMethod
    @TargetApi(23)
    public void forgetButton(String uuid, Callback successCallback) {
        Log.d(TAG, "forgetButton()");
        List<Flic2Button> buttons = manager.getButtons();
        for (Flic2Button button: buttons) {
            Log.d(TAG, "forgetButton() uuid: " + uuid +" " + button.getUuid());
            if (String.valueOf(uuid).equals(button.getUuid()))
            {
                button.disconnectOrAbortPendingConnection();
                manager.forgetButton(button);
                successCallback.invoke();
                return;
            }
        }
        successCallback.invoke();
        Log.d(TAG, "forgetButton() no button found " + uuid);
    }
    @ReactMethod
    @TargetApi(23)
    public void forgetAllButtons() {
        Log.d(TAG, "forgetAllButtons()");
        List<Flic2Button> buttons = manager.getButtons();
        for (Flic2Button button: buttons) {
            button.disconnectOrAbortPendingConnection();
            manager.forgetButton(button);
        }
    }
    @ReactMethod
    @TargetApi(23)
    public void startScan() {
//        if (!isScanning) {
//            int permissionCheck = ContextCompat.checkSelfPermission(mreactContext, Manifest.permission.ACCESS_FINE_LOCATION);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "scanNewButton() No permission");
//                return;
//            }
        Log.d(TAG, "startScan() start scan");
        isScanning = true;
        Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
            @Override
            public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                Log.d(TAG, "startScan() already paired button");
                mReactEvent.sendScanStatusMessage("alreadyPaired");
            }
            @Override
            public void onDiscovered(String bdAddr) {
                Log.d(TAG, "startScan() Found Flic2, now connecting...");
                mReactEvent.sendScanStatusMessage("discovered");
            }
            @Override
            public void onConnected() {
                Log.d(TAG, "startScan() Connected. Now pairing...");
                mReactEvent.sendScanStatusMessage("connected");
            }
            @Override
            public void onComplete(int result, int subCode, Flic2Button button) {
                isScanning = false;
                if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                    listenToButton(button);
                    mReactEvent.sendScanMessage("completion",false, result, button);
                } else {
                    mReactEvent.sendScanMessage("completion",true, result, button);
                }
            }
        });
//        }
    }
    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        Log.d(TAG, "isServiceRunning()");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            Log.d(TAG, "isServiceRunning()" +serviceClass.getName() + " " + service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "isServiceRunning() true");
                return true;
            }
        }
        Log.d(TAG, "isServiceRunning() false");
        return false;
    }

    static class ButtonData {
        Flic2Button button;
        boolean isDown;
        Flic2ButtonListener listener;

        public ButtonData(Flic2Button button) {
            this.button = button;
        }

    }
}