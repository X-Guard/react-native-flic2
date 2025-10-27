package nl.xguard.flic2.model;

import android.util.Log;

import io.flic.flic2libandroid.LoggerInterface;

public class ReactLogger implements LoggerInterface {
    private static final String TAG = ReactLogger.class.getSimpleName();

    @Override
    public void log(String bdAddr, String action, String text) {
        Log.d(TAG, "log() called with: bdAddr = [" + bdAddr + "], action = [" + action + "], text = [" + text + "]");
    }
}
