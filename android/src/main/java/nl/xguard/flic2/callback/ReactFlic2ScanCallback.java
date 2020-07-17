package nl.xguard.flic2.callback;

import android.util.Log;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ScanCallback;
import nl.xguard.flic2.communication.ReactEvent;
import nl.xguard.flic2.util.SisConsumer;

public class ReactFlic2ScanCallback implements Flic2ScanCallback {
    private static final String TAG = ReactFlic2ScanCallback.class.getSimpleName();

    SisConsumer<Flic2Button> mAddFlic2ButtonConsumer;

    public ReactFlic2ScanCallback(SisConsumer<Flic2Button> addFlic2ButtonConsumer) {
        mAddFlic2ButtonConsumer = addFlic2ButtonConsumer;
    }

    @Override
    public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
        Log.d(TAG, "onDiscoveredAlreadyPairedButton() called with: button = [" + button + "]");
        ReactEvent.getInstance().sendScanStatusMessage("alreadyPaired");
    }

    @Override
    public void onDiscovered(String bdAddr) {
        Log.d(TAG, "onDiscovered() called with: bdAddr = [" + bdAddr + "]");
        ReactEvent.getInstance().sendScanStatusMessage("discovered");
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected() called");
        ReactEvent.getInstance().sendScanStatusMessage("connected");
    }

    @Override
    public void onComplete(int result, int subCode, Flic2Button button) {
        Log.d(TAG, "onComplete() called with: result = [" + result + "], subCode = [" + subCode + "], button = [" + button + "]");
        if (result == Flic2ScanCallback.RESULT_SUCCESS) {
            mAddFlic2ButtonConsumer.accept(button);

            ReactEvent.getInstance().sendScanMessage("completion", false, result, button);
        } else {
            ReactEvent.getInstance().sendScanMessage("completion", true, result, button);
        }
    }
}
