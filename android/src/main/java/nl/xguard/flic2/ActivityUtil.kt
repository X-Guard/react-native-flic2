package nl.xguard.flic2

import android.app.ActivityManager
import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Build

object ActivityUtil {

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun startForegroundService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * True when Android refused starting a foreground service from the background.
     * Used so JS can defer initialize() until the app is active again.
     */
    fun isForegroundServiceStartBlocked(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && current is ForegroundServiceStartNotAllowedException
            ) {
                return true
            }

            val message = current.message.orEmpty()
            if (message.contains("ForegroundServiceStartNotAllowed")
                || message.contains("mAllowStartForeground")
                || message.contains("startForegroundService() not allowed")
            ) {
                return true
            }

            current = current.cause
        }
        return false
    }
}
