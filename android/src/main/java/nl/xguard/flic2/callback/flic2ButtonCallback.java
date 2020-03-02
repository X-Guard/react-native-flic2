package nl.xguard.flic2.callback;

import android.util.Log;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import nl.xguard.flic2.communication.ReactEvent;

import io.flic.flic2libandroid.BatteryLevel;
import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import nl.xguard.flic2.sharedpreferences.flic2SharedPreferences;

public class flic2ButtonCallback extends Flic2ButtonListener {

    private static final String TAG = "Flic2ButtonCallback";

    private ReactEvent mReactEvent;
//    private String packageName;
    private ReactContext mContext;

//    final private ReactInstanceManager mReactInstanceManager;
    public flic2ButtonCallback(ReactContext context, ReactInstanceManager instanceManager) {
        super();
        Log.d(TAG, "constructor()");
        mReactEvent = new ReactEvent(context);
        // mReactInstanceManager = instanceManager;
        // mContext = context;
//        packageName = packageName;
    }

//     private void checkAndRestart() {
//         Log.d(TAG, "checkAndRestart()"+ mReactInstanceManager.hasStartedCreatingInitialContext()+ " context" +mReactInstanceManager.getCurrentReactContext() + " " + mContext);
//         if (mReactInstanceManager.hasStartedCreatingInitialContext()) {
//             Log.d(TAG, "create background()");
// //            mReactInstanceManager.createReactContextInBackground();

//         } else {
//             if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
//                 flic2SharedPreferences sisSharedPreferences = flic2SharedPreferences.getInstance(mContext);
//                 Log.d(TAG, "check sis" + sisSharedPreferences.getBoolean(flic2SharedPreferences.PREF_KEY_IS_RUNNING));
//                 if (!sisSharedPreferences.getBoolean(flic2SharedPreferences.PREF_KEY_IS_RUNNING)) {
//                     Log.d(TAG, "recreate background() null");
// //                    mReactInstanceManager.recreateReactContextInBackground();
//                 }
//             }
//         }
//     }

    @Override
    public void onDisconnect(Flic2Button button) {
        Log.d(TAG, "onDisconnect() called with: button = [" + button + "]");
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_STATUS_DISCONNECTED);
    }
    
    @Override
    public void onConnect(Flic2Button button) {
        Log.d(TAG, "onConnect() called with: button = [" + button + "]");
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_STATUS_CONNECTION_COMPLETED);
    }

    @Override
    public void onFailure(Flic2Button button, int errorCode, int subCode) {
        Log.d(TAG, "onFailure() called with: button = [" + button + "]" + " error: " + errorCode);
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_STATUS_ON_FAILURE);
    }

    @Override
    public void onReady(Flic2Button button, long timestamp) {
        Log.d(TAG, "onReady() called with: button = [" + button + "]");
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_STATUS_CONNECTION_ON_READY);
    }

    @Override
    public void onUnpaired(Flic2Button button) {
        Log.d(TAG, "onUnpaired() called with: button = [" + button + "]");
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_STATUS_CONNECTION_UNPAIRED);
    }

    @Override
    public void onButtonClickOrHold(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isClick, boolean isHold) {
//        Log.d(TAG, "onButtonClickOrHold() called with: button = [" + button + "]");
//        if (isHold) {
//            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_HOLD, wasQueued, timestamp);
//        } else {
//            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_SINGLE_CLICK, wasQueued, timestamp);
//        }

    }

    @Override
    public void onButtonUpOrDown(final Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isUp, boolean isDown) {
        Log.d(TAG, "onButtonUpOrDown()");

        if (isDown) {
            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_DOWN, wasQueued, timestamp);
        }
        if (isUp) {
            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_UP, wasQueued, timestamp);
        }
    }

    @Override
    public void onButtonSingleOrDoubleClick(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick) {
//        if (isSingleClick) {
//            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_SINGLE_CLICK, wasQueued, timestamp);
//        } else {
//            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_DOUBLE_CLICK, wasQueued, timestamp);
//        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        if (isSingleClick) {
            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_SINGLE_CLICK, wasQueued, timestamp);
        } else if (isHold) {
            // checkAndRestart();
            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_HOLD, wasQueued, timestamp);
        } else {
            mReactEvent.send(button, ReactEvent.EVENT_BUTTON_DOUBLE_CLICK, wasQueued, timestamp);
        }
    }

    @Override
    public void onFirmwareVersionUpdated(Flic2Button button, int newVersion) {
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_NEW_FIRMWARE);
    }

    @Override
    public void onNameUpdated(Flic2Button button, String newName) {
        mReactEvent.send(button, ReactEvent.EVENT_BUTTON_NEW_NAME);
    }

    @Override
    public void onBatteryLevelUpdated(BatteryLevel level) {
        Log.d(TAG, "onBatteryLevelUpdated() called with: button = [" + level.getEstimatedPercentage() + "]");
//        mReactEvent.send(ReactEvent.EVENT_BUTTON_BATTERY_LEVEL, level.getEstimatedPercentage());
    }
}
