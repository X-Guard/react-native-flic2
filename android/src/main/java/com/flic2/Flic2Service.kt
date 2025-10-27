package com.flic2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
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

    companion object {
        private const val TAG = "Flic2Service"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "Flic2ServiceChannel"
        private const val CHANNEL_NAME = "Flic2 Service"
    }

    inner class Flic2ServiceBinder : Binder() {
        fun getService(): Flic2Service = this@Flic2Service
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

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

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Flic2 buttons connected in the background"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create notification
        val notification = createNotification()

        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification)

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

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, Flic2Service::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flic2 Service")
            .setContentText("Flic2 buttons are connected")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}

