package nl.xguard.flic2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import io.flic.flic2libandroid.Flic2Manager

class Flic2Service : Service() {

    private val binder = Flic2ServiceBinder()
    private var manager: Flic2Manager? = null
    private var isServiceStarted = false
    private var notification: Notification? = null

    companion object {
        private const val TAG = "Flic2Service"
        private const val DEFAULT_NOTIFICATION_ID = 123321
        private const val DEFAULT_CHANNEL_ID = "Notification_Channel_Flic2Service"

        // Metadata keys for notification configuration
        private const val KEY_CHANNEL_NAME = "nl.xguard.flic2.notification_channel_name"
        private const val KEY_CHANNEL_DESCRIPTION = "nl.xguard.flic2.notification_channel_description"
        private const val NOTIFICATION_TITLE_KEY = "nl.xguard.flic2.notification_title"
        private const val NOTIFICATION_TEXT_KEY = "nl.xguard.flic2.notification_text"
        private const val NOTIFICATION_ICON_KEY = "nl.xguard.flic2.notification_icon"
        private const val NOTIFICATION_ID_KEY = "nl.xguard.flic2.notification_id"
        private const val CHANNEL_ID_KEY = "nl.xguard.flic2.notification_channel_id"

        /**
         * Official flic2lib-android starts the FGS from Application.onCreate after boot/update
         * wake. We cannot hook the host Application, so receivers try startForegroundService here.
         * Modern Android may still block this; JS initialize() / app UI cover that path.
         */
        private fun tryStartFromBootOrUpdate(context: Context) {
            try {
                val appContext = context.applicationContext
                ActivityUtil.startForegroundService(
                    appContext,
                    Intent(appContext, Flic2Service::class.java)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start Flic2Service from boot/update receiver", e)
            }
        }
    }

    inner class Flic2ServiceBinder : Binder() {
        fun getService(): Flic2Service = this@Flic2Service
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        // Android requires startForeground() within ~5s of startForegroundService().
        // Promote immediately before Flic2Manager.init(), which can block the main thread.
        // Init stays synchronous in onCreate so bind/listeners still run after the manager is ready.
        createNotificationChannel()
        notification = createNotification()
        startForegroundService()

        try {
            // Initialize Flic2Manager on main thread with Handler
            // v1.1.0 API: init() returns void, must call getInstance() after
            Flic2Manager.init(
                applicationContext,
                Handler(Looper.getMainLooper())
            )
            manager = Flic2Manager.getInstance()
            Log.d(TAG, "Flic2Manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Flic2Manager", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d(TAG, "onStartCommand: ACTION_BOOT_COMPLETED")
            }
        }

        // Start foreground service if notification is ready
        if (notification != null) {
            startForegroundService()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service onBind")
        return binder
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        super.onDestroy()
    }

    fun getManager(): Flic2Manager? = manager

    fun isManagerInitialized(): Boolean = manager != null

    fun startForegroundService() {
        if (!isServiceStarted && notification != null) {
            isServiceStarted = true
            try {
                startForeground(getNotificationId(), notification)
            } catch (e: Exception) {
                Log.w(TAG, "startForegroundService() exception", e)
            }
        }
    }

    fun stopForegroundService() {
        if (isServiceStarted) {
            isServiceStarted = false
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getChannelId(),
                getChannelName(),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getChannelDescription()
                setShowBadge(false)
            }

            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, Flic2Service::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, getChannelId())
            .setContentTitle(getNotificationTitle())
            .setContentText(getNotificationText())
            .setSmallIcon(getNotificationIcon())
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun getNotificationId(): Int {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            metadata?.getInt(NOTIFICATION_ID_KEY, DEFAULT_NOTIFICATION_ID) ?: DEFAULT_NOTIFICATION_ID
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getNotificationId() NameNotFoundException", e)
            DEFAULT_NOTIFICATION_ID
        } catch (e: Exception) {
            Log.w(TAG, "getNotificationId() exception", e)
            DEFAULT_NOTIFICATION_ID
        }
    }

    private fun getChannelId(): String {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            metadata?.getString(CHANNEL_ID_KEY) ?: DEFAULT_CHANNEL_ID
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getChannelId() NameNotFoundException", e)
            DEFAULT_CHANNEL_ID
        } catch (e: Exception) {
            Log.w(TAG, "getChannelId() exception", e)
            DEFAULT_CHANNEL_ID
        }
    }

    private fun getChannelName(): String {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            metadata?.getString(KEY_CHANNEL_NAME) ?: "Flic2Channel"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getChannelName() NameNotFoundException", e)
            "Flic2Channel"
        } catch (e: Exception) {
            Log.w(TAG, "getChannelName() exception", e)
            "Flic2Channel"
        }
    }

    private fun getChannelDescription(): String {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            metadata?.getString(KEY_CHANNEL_DESCRIPTION) ?: "Flic2Channel"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getChannelDescription() NameNotFoundException", e)
            "Flic2Channel"
        } catch (e: Exception) {
            Log.w(TAG, "getChannelDescription() exception", e)
            "Flic2Channel"
        }
    }

    private fun getNotificationTitle(): String {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            metadata?.getString(NOTIFICATION_TITLE_KEY) ?: "Flic 2"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getNotificationTitle() NameNotFoundException", e)
            "Flic 2"
        } catch (e: Exception) {
            Log.w(TAG, "getNotificationTitle() exception", e)
            "Flic 2"
        }
    }

    private fun getNotificationText(): String {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            metadata?.getString(NOTIFICATION_TEXT_KEY) ?: "Flic 2 service is running"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getNotificationText() NameNotFoundException", e)
            "Flic 2 service is running"
        } catch (e: Exception) {
            Log.w(TAG, "getNotificationText() exception", e)
            "Flic 2 service is running"
        }
    }

    private fun getNotificationIcon(): Int {
        return try {
            val metadata = applicationContext.packageManager
                .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                .metaData
            val icon = metadata?.getInt(NOTIFICATION_ICON_KEY, 0) ?: 0
            if (icon != 0) icon else android.R.drawable.ic_dialog_info
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "getNotificationIcon() NameNotFoundException", e)
            android.R.drawable.ic_dialog_info
        } catch (e: Exception) {
            Log.w(TAG, "getNotificationIcon() exception", e)
            android.R.drawable.ic_dialog_info
        }
    }

    // Wake process on boot / package replace, then try to start Flic2Service (FGS).
    // Application.onCreate has already run; JS initialize() is still required for the RN bridge.
    class BootUpReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "BootUpReceiver()")
            tryStartFromBootOrUpdate(context)
        }
    }

    class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "UpdateReceiver()")
            tryStartFromBootOrUpdate(context)
        }
    }
}

