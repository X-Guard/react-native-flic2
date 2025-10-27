package nl.xguard.flic2.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;


public class Flic2ServiceConnection implements ServiceConnection {

    private static final String TAG = Flic2ServiceConnection.class.getSimpleName();

    private AtomicBoolean mIsServiceConnected = new AtomicBoolean();
    private BehaviorSubject<IFlic2Service> mFlic2ServiceSubject = BehaviorSubject.create();

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mIsServiceConnected.set(true);

        IFlic2Service flic2Service = ((Flic2Service.Flic2ServiceBinder) service).getService();
        mFlic2ServiceSubject.onNext(flic2Service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mIsServiceConnected.set(false);
    }

    public Observable<IFlic2Service> getFlic2ServiceObservable() {
        return mFlic2ServiceSubject;
    }

    public boolean isServiceConnected() {
        return mIsServiceConnected.get();
    }
}
