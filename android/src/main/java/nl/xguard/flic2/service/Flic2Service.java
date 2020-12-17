package nl.xguard.flic2.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.os.Bundle;

import androidx.core.app.NotificationCompat.Builder;

import java.util.Objects;

import io.flic.flic2libandroid.Flic2Manager;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import nl.xguard.flic2.R;
import nl.xguard.flic2.model.ReactAndroidHandler;
import nl.xguard.flic2.model.ReactLogger;


public class Flic2Service extends Service implements IFlic2Service {

    private static final String TAG = Flic2Service.class.getSimpleName();

    private IBinder mFlic2ServiceBinder = new Flic2ServiceBinder();
    private static final int SERVICE_NOTIFICATION_ID = 123321;
    private final String NOTIFICATION_CHANNEL_ID = "Notification_Channel_Flic2Service";
    private final CharSequence NOTIFICATION_CHANNEL_NAME = "Flic2Channel";
    private static final String NOTIFICATION_TITLE_KEY = "nl.xguard.flic2.notification_title";
    private static final String NOTIFICATION_TEXT_KEY = "nl.xguard.flic2.notification_text";
    private static final String NOTIFICATION_ICON_KEY = "nl.xguard.flic2.notification_icon";
    
    private Notification notification;
    private String notificationTitle = "Flic 2";
    private String notificationText = "Flic 2 service is running";
    private int notificationIcon = R.mipmap.ic_launcher;
    private boolean foregroundRunning = false;

    private BehaviorSubject<Boolean> mIsFlic2InitSubject = BehaviorSubject.create();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        try {
            Context context = getApplicationContext();
            Bundle metadata = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;

            String title = metadata.getString(NOTIFICATION_TITLE_KEY);
            if (title != null) {
              notificationTitle = title;
            }

            String text = metadata.getString(NOTIFICATION_TEXT_KEY);
            if (text != null) {
              notificationText = text;
            }

            int icon = metadata.getInt(NOTIFICATION_ICON_KEY);
            if (icon != 0) {
              notificationIcon = icon;
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "onCreate(), NameNotFoundException", e);
        }
        
        Flic2Manager.init(getApplicationContext(), new ReactAndroidHandler(new Handler()), new ReactLogger());
        setFlic2Init();

        Intent notificationIntent = new Intent(this, Flic2Service.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }

        notification = new Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(notificationIcon)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();

        this.startForegroundService();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()" + this.foregroundRunning);

        this.stopForegroundService();
        stopSelf();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mFlic2ServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

        if (intent != null) {
            if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
                Log.d(TAG, "onStartCommand: ACTION_BOOT_COMPLETED");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void setFlic2Init() {
      Log.d(TAG, "setFlic2Init");
        mIsFlic2InitSubject.onNext(true);
    }

    @Override
    public Observable<Boolean> flic2IsInitialized() {
      Log.d(TAG, "setFlic2Init" +mIsFlic2InitSubject);
      return mIsFlic2InitSubject;
    }

    public static class BootUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BootUpReceiver()");
            // The Application class's onCreate has already been called at this point, which is what we want
        }
    }

    public static class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "UpdateReceiver()");
            // The Application class's onCreate has already been called at this point, which is what we want
        }
    }

    public class Flic2ServiceBinder extends Binder {
        IFlic2Service getService() {
            return Flic2Service.this;
        }
    }

    @Override
    public void startForegroundService() {
      Log.d(TAG, "startForegroundService() isrunning? " +this.foregroundRunning);

      if (this.foregroundRunning == false) {
        this.foregroundRunning = true;

        startForeground(SERVICE_NOTIFICATION_ID, notification);
      }

    }

    @Override
    public void stopForegroundService() {

      Log.d(TAG, "stopForegroundService() isrunning? " +this.foregroundRunning);

      if (this.foregroundRunning == true) {
        this.foregroundRunning = false;

        stopForeground(true);
      }

    }
}

