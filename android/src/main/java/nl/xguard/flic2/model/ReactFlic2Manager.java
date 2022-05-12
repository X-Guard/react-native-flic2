package nl.xguard.flic2.model;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.ArrayList;
import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.util.Consumer;
import nl.xguard.flic2.service.IFlic2Service;

public class ReactFlic2Manager implements IReactFlic2Manager {
    private static final String TAG = ReactFlic2Manager.class.getSimpleName();

    private Flic2Manager mFlic2Manager;
    private ReactFlic2ButtonListener mReactFlic2ButtonListener;
    private ArrayList<Flic2Button> mRegisteredFlic2Buttons = new ArrayList<>();
    private IFlic2Service mFlic2Service;
    
    private Context mContext;
    private BluetoothReceiver mReceiver;

    public ReactFlic2Manager(Flic2Manager flic2Manager, ReactFlic2ButtonListener reactFlic2ButtonListener, IFlic2Service flic2Service, Context mContext) {
        mFlic2Manager = flic2Manager;
        mReactFlic2ButtonListener = reactFlic2ButtonListener;
        mFlic2Service = flic2Service;

        if (mReceiver == null) {
            mContext = mContext;
            mReceiver = new BluetoothReceiver();
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void startScan() {
        mFlic2Manager.startScan(new ReactFlic2ScanCallback(this::registerFlic2Button));
    }

    @Override
    public void stopScan() {
        mFlic2Manager.stopScan();
    }

    @Override
    public void initButtons() {
        for (Flic2Button flic2Button : mFlic2Manager.getButtons()) {
            registerFlic2Button(flic2Button);
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mRegisteredFlic2Buttons.size() == 0 || !mBluetoothAdapter.isEnabled()) {
          mFlic2Service.stopForegroundService();
        }
    }

    @Override
    public void connectAllButtons() {
        updateButtons(this::connectButton);
    }

    @Override
    public void disconnectAllKnownButtons() {
        updateButtons(this::disconnectButton);
    }

    @Override
    public void forgetAllButtons() {
        updateButtons(this::forgetButton);
    }

    @Override
    public void connectButton(String uuid) {
        Log.w(TAG, "connectButton() called with: size = [" + mRegisteredFlic2Buttons.size() + "]");
        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            connectButton(flic2Button);
        }
    }

    @Override
    public void setName(String uuid, String name) {
        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            flic2Button.setName(name);
        }
    }

    @Override
    public void disconnectButton(String uuid) {
        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            disconnectButton(flic2Button);
        }
    }

    @Override
    public void forgetButton(String uuid) {
        Flic2Button flic2Button = getButton(uuid);
        if (flic2Button != null) {
            forgetButton(flic2Button);
        }
    }

    @Override
    public WritableArray getButtons() {
        WritableArray array = new WritableNativeArray();

        for (Flic2Button button : mRegisteredFlic2Buttons) {
            WritableMap map = ReactEvent.getInstance().getButtonArgs(button);
            array.pushMap(map);
        }
        return array;
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

    private void updateButtons(Consumer<Flic2Button> consumer) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter.isEnabled()) {
            for (Flic2Button flic2Button : mRegisteredFlic2Buttons) {
                consumer.accept(flic2Button);
            }
        }
    }

    private void registerFlic2Button(Flic2Button flic2Button) {
        flic2Button.addListener(mReactFlic2ButtonListener);
        mRegisteredFlic2Buttons.add(flic2Button);
        if (mRegisteredFlic2Buttons.size() > 0) {
            mFlic2Service.startForegroundService();
        }
    }

    private void unregisterFlic2Button(Flic2Button flic2Button) {
        mRegisteredFlic2Buttons.remove(flic2Button);
        flic2Button.removeListener(mReactFlic2ButtonListener);
        if (mRegisteredFlic2Buttons.size() == 0) {
            mFlic2Service.stopForegroundService();
        }
    }

    private void connectButton(Flic2Button flic2Button) {
        flic2Button.connect();
        if (mRegisteredFlic2Buttons.size() > 0) {
            mFlic2Service.startForegroundService();
        }
    }

    private void disconnectButton(Flic2Button flic2Button) {
        flic2Button.disconnectOrAbortPendingConnection();
    }

    private void forgetButton(Flic2Button flic2Button) {
        disconnectButton(flic2Button);
        unregisterFlic2Button(flic2Button);
        mFlic2Manager.forgetButton(flic2Button);
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF: {
                    Log.d(TAG, "BluetoothReceiver: off - " + mRegisteredFlic2Buttons.size());
                    mFlic2Service.stopForegroundService();
                    break;
                }
                case BluetoothAdapter.STATE_ON: {
                    connectAllButtons();   
                    Log.d(TAG, "BluetoothReceiver: on - " + mRegisteredFlic2Buttons.size());
                    if (mRegisteredFlic2Buttons.size() > 0) {
                        mFlic2Service.startForegroundService();
                    }
                    break;
                }
            }
        }
    }

}
