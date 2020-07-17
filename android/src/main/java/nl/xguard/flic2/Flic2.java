package nl.xguard.flic2;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.ArrayList;
import java.util.List;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.model.ReactFlic2Button;

public class Flic2 extends ReactContextBaseJavaModule {
    private static final String TAG = Flic2.class.getSimpleName();

    private Flic2Manager mFlic2Manager;
    private boolean managerIsReady;
    private ArrayList<ReactFlic2Button> mReactFlic2Buttons = new ArrayList<>();

    @Deprecated
    private boolean isScanning;


    @Override
    public String getName() {
        return Flic2.class.getSimpleName();
    }

    @ReactMethod
    public Flic2(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d(TAG, "onCreate()");
        managerIsReady = false;

        try {
            mFlic2Manager = Flic2Manager.getInstance();
            managerIsReady = true;
        } catch (Exception e) {
            managerIsReady = false;
        }

        ReactEvent.createInstance(reactContext);

        if (mFlic2Manager == null) {
            Log.e(TAG, "nl.xguard.flic2.Flic2: manager is null");
            return;
        }
        for (Flic2Button button : mFlic2Manager.getButtons()) {
            registerFlic2Button(button);
        }

    }


    @ReactMethod
    @TargetApi(23)
    public void startup() {
        // do nothing
    }

    public static void startupAndroid(Context context, Handler handler) {
        Log.d(TAG, "startup()");
        Flic2Manager.initAndGetInstance(context, handler);
    }

    @ReactMethod
    @TargetApi(23)
    public void startService() {
//        Log.d(TAG, "startService()");
//        Boolean isRunning = isServiceRunning(mreactContext, Flic2Service.class);
//
//        if (!isRunning) {
//            Intent intent = new Intent(mreactContext, Flic2Service.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.d(TAG, "startService() new");
//                mreactContext.startForegroundService(intent);
//            } else {
//                Log.d(TAG, "startService() old" );
//                mreactContext.startService(intent);
//            }
//
//        } else {
//            Log.d(TAG, "startService(): service is already running");
//        }
    }

    public void registerFlic2Button(Flic2Button flic2Button) {
        Log.d(TAG, "registerFlic2Button() called with: flic2Button = [" + flic2Button + "]");
        final ReactFlic2Button reactFlic2Button = new ReactFlic2Button(flic2Button);
        mReactFlic2Buttons.add(reactFlic2Button);
    }

    @ReactMethod
    @TargetApi(23)
    public void connectAllKnownButtons() {
        Log.d(TAG, "connectAllKnownButtons()");
        for (Flic2Button button : mFlic2Manager.getButtons()) {
            button.connect();
        }
    }

    @ReactMethod
    @TargetApi(23)
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
    @TargetApi(23)
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
    @TargetApi(23)
    public void disconnectAllKnownButtons() {
        Log.d(TAG, "disconnectAllKnownButtons()");
        for (Flic2Button button : mFlic2Manager.getButtons()) {
            button.disconnectOrAbortPendingConnection();
        }
    }

    @ReactMethod
    @TargetApi(23)
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
    @TargetApi(23)
    public void stopScan() {
        Log.d(TAG, "stopScanning()");
        mFlic2Manager.stopScan();
        isScanning = false;
    }

    @ReactMethod
    @TargetApi(23)
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
    @TargetApi(23)
    public void forgetButton(String uuid, Callback successCallback) {
        Log.d(TAG, "forgetButton()");
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
    @TargetApi(23)
    public void forgetAllButtons() {
        Log.d(TAG, "forgetAllButtons()");
        List<Flic2Button> buttons = mFlic2Manager.getButtons();
        for (Flic2Button button : buttons) {
            button.disconnectOrAbortPendingConnection();
            mFlic2Manager.forgetButton(button);
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
        if (managerIsReady) {
            mFlic2Manager.startScan(new Flic2ScanCallback() {
                @Override
                public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                    Log.d(TAG, "startScan() already paired button");
                    ReactEvent.getInstance().sendScanStatusMessage("alreadyPaired");
                }

                @Override
                public void onDiscovered(String bdAddr) {
                    Log.d(TAG, "startScan() Found nl.xguard.flic2.Flic2, now connecting...");
                    ReactEvent.getInstance().sendScanStatusMessage("discovered");
                }

                @Override
                public void onConnected() {
                    Log.d(TAG, "startScan() Connected. Now pairing...");
                    ReactEvent.getInstance().sendScanStatusMessage("connected");
                }

                @Override
                public void onComplete(int result, int subCode, Flic2Button button) {
                    isScanning = false;
                    if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                        registerFlic2Button(button);

                        ReactEvent.getInstance().sendScanMessage("completion", false, result, button);
                    } else {
                        ReactEvent.getInstance().sendScanMessage("completion", true, result, button);
                    }
                }
            });
        }

    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        Log.d(TAG, "isServiceRunning()");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            Log.d(TAG, "isServiceRunning()" + serviceClass.getName() + " " + service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "isServiceRunning() true");
                return true;
            }
        }
        Log.d(TAG, "isServiceRunning() false");
        return false;
    }
}