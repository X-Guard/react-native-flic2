package nl.xguard.flic2.model;

import android.util.Log;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

public class NullReactFlic2Manager implements IReactFlic2Manager {
    private static final String TAG = NullReactFlic2Manager.class.getSimpleName();

    @Override
    public void startScan() {
        logNullReactFlic2Manager();
    }

    @Override
    public void stopScan() {
        logNullReactFlic2Manager();
    }

    @Override
    public void initButtons() {
        logNullReactFlic2Manager();
    }

    @Override
    public void connectAllButtons() {
        logNullReactFlic2Manager();
    }

    @Override
    public void disconnectAllKnownButtons() {
        logNullReactFlic2Manager();
    }

    @Override
    public void forgetAllButtons() {
        logNullReactFlic2Manager();
    }

    @Override
    public void connectButton(String uuid) {
        logNullReactFlic2Manager();
    }

    @Override
    public void setName(String uuid, String name) {
        logNullReactFlic2Manager();
    }

    @Override
    public void disconnectButton(String uuid) {
        logNullReactFlic2Manager();
    }

    @Override
    public void forgetButton(String uuid) {
        logNullReactFlic2Manager();
    }

    @Override
    public WritableArray getButtons() {
        logNullReactFlic2Manager();
        return new WritableNativeArray();
    }

    private void logNullReactFlic2Manager() {
        Log.e(TAG, "logNullReactFlic2Manager: The Flic2Manager is not yet initialized, check if the Flic2Service is running");
    }
}
