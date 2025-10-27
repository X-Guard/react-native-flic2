package nl.xguard.flic2.model;

import android.os.Handler;

import io.flic.flic2libandroid.HandlerInterface;

public class ReactAndroidHandler implements HandlerInterface {
    private Handler handler;

    public ReactAndroidHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void post(Runnable r) {
        handler.post(r);
    }

    @Override
    public void postDelayed(Runnable r, long delay) {
        handler.postDelayed(r, delay);
    }

    @Override
    public void removeCallbacks(Runnable r) {
        handler.removeCallbacks(r);
    }

    @Override
    public boolean currentThreadIsHandlerThread() {
        return Thread.currentThread() == handler.getLooper().getThread();
    }
}