package nl.xguard.flic2.communication;

import com.facebook.react.bridge.WritableMap;

import io.flic.flic2libandroid.Flic2Button;

public interface IReactEvent {
    void send(String event, String value);
    void send(Flic2Button button, String value);
    void send(Flic2Button button, String event, Boolean queued, long age);
    WritableMap getButtonArgs(Flic2Button button);

    void sendScanStatusMessage(String event);
    void sendScanMessage(String event, Boolean error, int code, Flic2Button button);
}
