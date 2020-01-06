package nl.xguard.flic2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import nl.xguard.flic2.callback.flic2ButtonCallback;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.service.Flic2Service;

import java.util.List;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import io.flic.flic2libandroid.HandlerInterface;
import io.flic.flic2libandroid.LoggerInterface;

public class Flic2Module extends ReactContextBaseJavaModule implements HandlerInterface, LoggerInterface {

    private static final String TAG = "Flic2Module";
    private ReactApplicationContext mreactContext;
    private Flic2Manager manager;
    private ReactEvent mReactEvent;
    private boolean isScanning;

    @Override
    public String getName() {
        return Flic2Module.class.getSimpleName();
    }


    @ReactMethod
    public Flic2Module(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d(TAG, "onCreate()");

        mreactContext = reactContext;
        mReactEvent = new ReactEvent(mreactContext);

        Looper.prepare();

    }


    @ReactMethod
    public void startup() {

        Log.d(TAG, "startup()");

        Flic2Manager.init(mreactContext, new Handler(Looper.getMainLooper()));

        manager = Flic2Manager.getInstance();

        mReactEvent.send(ReactEvent.EVENT_MANAGER_IS_INITIALIZED);

        connectAllKnownButtons();
    }


    @ReactMethod
    @TargetApi(23)
    public void startService() {

        Boolean isRunning = isServiceRunning(getReactApplicationContext(), Flic2Service.class);

        if (!isRunning) {

            Log.d(TAG, "startService()");

            Intent intent = new Intent(getReactApplicationContext(), Flic2Service.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getReactApplicationContext().startForegroundService(intent);
            } else {
                getReactApplicationContext().startService(intent);
            }

        } else {
            Log.d(TAG, "startService(): service is already running");
        }

    }



    public void listenToButtonWithToast(Flic2Button button) {
        button.addListener(new flic2ButtonCallback(mreactContext));
    }

    @ReactMethod
    @TargetApi(23)
    public void connectAllKnownButtons() {
        for (Flic2Button button : manager.getButtons()) {
            button.connect();
            listenToButtonWithToast(button);
        }
    }

    @ReactMethod
    @TargetApi(23)
    public void stopScanning() {
        manager.stopScan();
        isScanning = false;
    }

    @ReactMethod
    @TargetApi(23)
    public void getButtons(Promise promise) {
        try {
            List<Flic2Button> buttons = manager.getButtons();
            WritableArray array = new WritableNativeArray();
            for (Flic2Button button: buttons) {
                WritableMap map = mReactEvent.getButtonArgs(button);
                map.putString("serial", button.getSerialNumber());
                array.pushMap(map);
            }
            promise.resolve(array);
        }  catch (Exception e) {
            promise.reject("Error getting buttons", e.getMessage());
        }

    }

    @ReactMethod
    @TargetApi(23)
    public void forgetButton(String uuid) {

        List<Flic2Button> buttons = manager.getButtons();

        for (Flic2Button button: buttons) {

            Log.d(TAG, "forgetButton() uuid: " + uuid +" " + button.getUuid());

            if (String.valueOf(uuid).equals(button.getUuid()))
            {
                manager.forgetButton(button);
                return;
            }
        }

        Log.d(TAG, "forgetButton() no button found " + uuid);

    }

    @ReactMethod
    @TargetApi(23)
    public void forgetAllButtons() {
        Log.d(TAG, "forgetAllButtons()");

        List<Flic2Button> buttons = manager.getButtons();

        for (Flic2Button button: buttons) {
            manager.forgetButton(button);
        }

    }

    @ReactMethod
    @TargetApi(23)
    public void isScanning(Promise promise) {
        promise.resolve(this.isScanning);
    }


    @ReactMethod
    @TargetApi(23)
    public void startScan() {
        if (!isScanning) {
            int permissionCheck = ContextCompat.checkSelfPermission(mreactContext, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "scanNewButton() No permission");
                return;
            }

            Log.d(TAG, "startScan() start scan");

            isScanning = true;

            Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
                @Override
                public void onDiscoveredAlreadyPairedButton(Flic2Button button) {

                    Log.d(TAG, "startScan() already paired button");
                }

                @Override
                public void onDiscovered(String bdAddr) {

                    Log.d(TAG, "startScan() Found Flic2, now connecting...");
                }

                @Override
                public void onConnected() {
                    Log.d(TAG, "startScan() Connected. Now pairing...");
                }

                @Override
                public void onComplete(int result, int subCode, Flic2Button button) {
                    isScanning = false;


                    if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                        Log.d(TAG, "startScan() Scan wizard success!");
                        listenToButtonWithToast(button);
                    } else {
                        Log.d(TAG, "startScan() Scan wizard failed with code " + Flic2Manager.errorCodeToString(result));
                    }
                }
            });
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void post(Runnable r) {
//        this.post(r);
    }

    @Override
    public void postDelayed(Runnable r, long delayMillis) {
//        this.postDelayed(r, delayMillis);
    }

    @Override
    public void removeCallbacks(Runnable r) {
//        this.removeCallbacks(r);
    }

    @Override
    public boolean currentThreadIsHandlerThread() {

        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    @Override
    public void log(String bdAddr, String Action, String text) {
        Log.d(TAG, "log()");
    }


}
