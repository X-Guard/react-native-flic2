package nl.xguard.flic2.model;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import nl.xguard.flic2.callback.ReactFlic2ButtonListener;

public class ReactFlic2Button {
    private Flic2Button mFlic2Button;
    private Flic2ButtonListener mFlic2ButtonListener;

    public ReactFlic2Button(Flic2Button flic2Button) {
        mFlic2Button = flic2Button;
        registerListener();
    }

    public void registerListener() {
        mFlic2ButtonListener = new ReactFlic2ButtonListener();
        mFlic2Button.addListener(mFlic2ButtonListener);
    }

    public void unregisterListener() {
        mFlic2Button.removeListener(mFlic2ButtonListener);
    }

}
