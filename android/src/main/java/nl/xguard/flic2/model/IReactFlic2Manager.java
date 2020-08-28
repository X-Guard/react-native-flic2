package nl.xguard.flic2.model;

import com.facebook.react.bridge.WritableArray;

public interface IReactFlic2Manager {
    void startScan();

    void stopScan();

    void initButtons();

    void connectAllButtons();

    void disconnectAllKnownButtons();

    void forgetAllButtons();

    void connectButton(String uuid);

    void setName(String uuid, String name);

    void disconnectButton(String uuid);

    void forgetButton(String uuid);

    WritableArray getButtons();
}
