package nl.xguard.flic2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import io.flic.flic2libandroid.Flic2Button
import io.flic.flic2libandroid.Flic2Manager
import io.flic.flic2libandroid.Flic2ScanCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Custom exception for scan errors with error codes
class ScanException(val errorCode: String, val code: Int, message: String) : Exception(message)

@ReactModule(name = Flic2Module.NAME)
class Flic2Module(reactContext: ReactApplicationContext) :
  NativeFlic2Spec(reactContext) {

  private var flic2Service: Flic2Service? = null
  private var serviceBound = false
  private val moduleScope = CoroutineScope(Dispatchers.Main + Job())
  private var scanJob: Job? = null
  private val buttonListeners = mutableMapOf<String, Flic2ButtonEventListener>()
  private var initializePromise: Promise? = null

  companion object {
    const val NAME = "Flic2"
    private const val TAG = "Flic2Module"
  }

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      Log.d(TAG, "Service connected")
      val binder = service as Flic2Service.Flic2ServiceBinder
      flic2Service = binder.getService()
      serviceBound = true

      // Set up listeners for existing buttons
      flic2Service?.getManager()?.let { manager ->
        manager.buttons.forEach { button ->
          setupButtonListener(button)
        }
        // Update foreground service state based on button count
        updateForegroundServiceState(manager.buttons.size)
      }

      // Resolve the initialize promise if pending
      initializePromise?.let { promise ->
        promise.resolve(Arguments.createMap().apply {
          putBoolean("success", true)
          putString("message", "Manager initialized successfully")
        })
        initializePromise = null
      }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
      Log.d(TAG, "Service disconnected")
      serviceBound = false
      flic2Service = null

      // Reject any pending initialize promise
      initializePromise?.let { promise ->
        promise.reject("SERVICE_DISCONNECTED", "Service disconnected unexpectedly")
        initializePromise = null
      }
    }
  }

  override fun getName(): String {
    return NAME
  }

  override fun invalidate() {
    super.invalidate()
    moduleScope.cancel()
    if (serviceBound) {
      reactApplicationContext.unbindService(serviceConnection)
      serviceBound = false
    }
  }

  // MARK: - Manager Methods

  override fun initialize(background: Boolean, promise: Promise) {
    try {
      // Store the promise to resolve when service is connected
      initializePromise = promise

      val intent = Intent(reactApplicationContext, Flic2Service::class.java)

      // Check if service is already running
      val isRunning = ActivityUtil.isServiceRunning(reactApplicationContext, Flic2Service::class.java)
      if (!isRunning) {
        // Start service
        ActivityUtil.startForegroundService(reactApplicationContext, intent)
      }

      // Bind to service - promise will be resolved in onServiceConnected
      val bound = reactApplicationContext.bindService(
        intent,
        serviceConnection,
        Context.BIND_AUTO_CREATE
      )

      if (!bound) {
        initializePromise = null
        promise.reject("INIT_ERROR", "Failed to bind to service")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize", e)
      initializePromise = null
      promise.reject("INIT_ERROR", "Failed to initialize: ${e.message}", e)
    }
  }

  override fun getButtons(promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      val buttons = manager.buttons
      val buttonArray = Flic2Converter.buttonsToArray(buttons)
      promise.resolve(buttonArray)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to get buttons", e)
      promise.reject("GET_BUTTONS_ERROR", "Failed to get buttons: ${e.message}", e)
    }
  }

  override fun scanForButtons(promise: Promise) {
    val manager = flic2Service?.getManager()
    if (manager == null) {
      promise.reject("NOT_INITIALIZED", "Manager not initialized")
      return
    }

    // Cancel any existing scan
    scanJob?.cancel()

    Log.d(TAG, "Starting scan")

    // Emit started event (matches iOS)
    emitOnScanStatusChange(Arguments.createMap().apply {
      putString("event", "started")
      putString("eventName", "started")
    })

    manager.startScan(object : Flic2ScanCallback {
      override fun onDiscoveredAlreadyPairedButton(button: Flic2Button) {
        Log.d(TAG, "Discovered already paired button")
      }

      override fun onDiscovered(bdAddr: String) {
        Log.d(TAG, "Discovered button: $bdAddr")
      }

      override fun onConnected() {
        Log.d(TAG, "Button connected during scan")
      }

      override fun onComplete(result: Int, subCode: Int, button: Flic2Button?) {
        Log.d(TAG, "Scan complete: result=$result, button=${button?.uuid}")

        val resultCode = mapScanResultToCode(result)

        if (result == Flic2ScanCallback.RESULT_SUCCESS && button != null) {
          // Auto-connect (trigger mode not available in Android v1.1.0+)
          button.connect()

          setupButtonListener(button)

          // Update foreground service state after adding button
          flic2Service?.getManager()?.let { manager ->
            updateForegroundServiceState(manager.buttons.size)
          }

          // Emit discovered event as button event (like iOS)
          emitOnButtonEvent(Arguments.createMap().apply {
            putString("uuid", button.uuid)
            putString("event", "discovered")
            putMap("button", Flic2Converter.buttonToMap(button))
          })
        } else {
          val errorCode = Flic2Converter.scanResultToString(result)
          Log.e(TAG, "Scan failed with error code: $errorCode")
        }

        // Emit scan completion with result code
        emitOnScanStatusChange(Arguments.createMap().apply {
          putString("event", "completion")
          putString("eventName", "completion")
          putInt("result", resultCode)
        })
      }
    })

    // Return immediately - scan results will come through events
    promise.resolve(Arguments.createMap().apply {
      putBoolean("success", true)
      putString("message", "Scan started")
    })
  }

  override fun stopScan(promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      scanJob?.cancel()
      manager.stopScan()

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "Scan stopped")
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to stop scan", e)
      promise.reject("STOP_SCAN_ERROR", "Failed to stop scan: ${e.message}", e)
    }
  }

  override fun forgetButton(uuid: String, promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      val button = manager.buttons.find { it.uuid == uuid }
      if (button == null) {
        promise.reject("BUTTON_NOT_FOUND", "Button not found")
        return
      }

      // Disconnect before forgetting like iOS
      button.disconnectOrAbortPendingConnection()

      // Remove listener
      buttonListeners.remove(uuid)

      // Forget button
      manager.forgetButton(button)

      // Update foreground service state after removing button
      updateForegroundServiceState(manager.buttons.size)

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "Button forgotten")
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to forget button", e)
      promise.reject("FORGET_ERROR", "Failed to forget button: ${e.message}", e)
    }
  }

  // MARK: - Button Methods

  override fun connectButton(uuid: String, promise: Promise) {
    try {
      val button = findButton(uuid)
      if (button == null) {
        promise.reject("BUTTON_NOT_FOUND", "Button not found")
        return
      }

      button.connect()

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "Connection initiated")
        putMap("button", Flic2Converter.buttonToMap(button))
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to connect button", e)
      promise.reject("CONNECT_ERROR", "Failed to connect: ${e.message}", e)
    }
  }

  override fun disconnectButton(uuid: String, promise: Promise) {
    try {
      val button = findButton(uuid)
      if (button == null) {
        promise.reject("BUTTON_NOT_FOUND", "Button not found")
        return
      }

      button.disconnectOrAbortPendingConnection()

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "Disconnection initiated")
        putMap("button", Flic2Converter.buttonToMap(button))
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to disconnect button", e)
      promise.reject("DISCONNECT_ERROR", "Failed to disconnect: ${e.message}", e)
    }
  }

  override fun setTriggerMode(uuid: String, mode: Double, promise: Promise) {
    promise.reject(
        "NOT_SUPPORTED_ON_ANDROID",
        "Trigger mode is only supported on iOS. Android Flic2 library v1.1.0+ does not support trigger modes."
    )
  }

  override fun setLatencyMode(uuid: String, mode: Double, promise: Promise) {
    promise.reject(
        "NOT_SUPPORTED_ON_ANDROID",
        "Latency mode is only supported on iOS. Android Flic2 library v1.1.0+ does not support latency modes."
    )
  }

  override fun setNickname(uuid: String, nickname: String, promise: Promise) {
    try {
      val button = findButton(uuid)
      if (button == null) {
        promise.reject("BUTTON_NOT_FOUND", "Button not found")
        return
      }

      // v1.1.0 uses setName() method instead of property
      button.setName(nickname)

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "Nickname set")
        putMap("button", Flic2Converter.buttonToMap(button))
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to set nickname", e)
      promise.reject("SET_NICKNAME_ERROR", "Failed to set nickname: ${e.message}", e)
    }
  }

  override fun connectAllKnownButtons(promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      val buttons = manager.buttons

      buttons.forEach { button ->
        Log.d(TAG, "Connecting button: ${button.getName()}")
        // Trigger mode not available in Android v1.1.0+
        button.connect()
      }

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "All buttons connection initiated")
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to connect all buttons", e)
      promise.reject("CONNECT_ALL_ERROR", "Failed to connect all buttons: ${e.message}", e)
    }
  }

  override fun disconnectAllKnownButtons(promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      val buttons = manager.buttons

      buttons.forEach { button ->
        Log.d(TAG, "Disconnecting button: ${button.name}")
        button.disconnectOrAbortPendingConnection()
      }

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "All buttons disconnection initiated")
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to disconnect all buttons", e)
      promise.reject("DISCONNECT_ALL_ERROR", "Failed to disconnect all buttons: ${e.message}", e)
    }
  }

  override fun forgetAllButtons(promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      // Create a copy of the list to avoid concurrent modification
      val buttons = manager.buttons.toList()

      buttons.forEach { button ->
        buttonListeners.remove(button.uuid)
        button.disconnectOrAbortPendingConnection()
        manager.forgetButton(button)
      }

      // Update foreground service state after removing all buttons
      updateForegroundServiceState(manager.buttons.size)

      promise.resolve(Arguments.createMap().apply {
        putBoolean("success", true)
        putString("message", "All buttons forgotten")
      })
    } catch (e: Exception) {
      Log.e(TAG, "Failed to forget all buttons", e)
      promise.reject("FORGET_ALL_ERROR", "Failed to forget all buttons: ${e.message}", e)
    }
  }

  override fun isScanning(promise: Promise) {
    try {
      val manager = flic2Service?.getManager()
      if (manager == null) {
        promise.reject("NOT_INITIALIZED", "Manager not initialized")
        return
      }

      val scanning = (scanJob != null && scanJob?.isActive == true)
      promise.resolve(scanning)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to check scanning status", e)
      promise.reject("IS_SCANNING_ERROR", "Failed to check scanning status: ${e.message}", e)
    }
  }

  // MARK: - Helper Methods

  private fun findButton(uuid: String): Flic2Button? {
    return flic2Service?.getManager()?.buttons?.find { it.uuid == uuid }
  }

  private fun setupButtonListener(button: Flic2Button) {
    // Remove existing listener if any
    buttonListeners.remove(button.uuid)

    // Create new listener
    val listener = Flic2ButtonEventListener { event ->
      emitOnButtonEvent(event)
    }

    // Add listener to button
    button.addListener(listener)

    // Store listener reference
    buttonListeners[button.uuid] = listener
  }

  private fun updateForegroundServiceState(buttonCount: Int) {
    if (buttonCount > 0) {
      // Start foreground service when buttons exist
      flic2Service?.startForegroundService()
    } else {
      // Stop foreground service when no buttons
      flic2Service?.stopForegroundService()
    }
  }

  private fun mapScanResultToCode(result: Int): Int {
    // Map Android library's 9 result codes (0-8) to TypeScript enum codes (0-21) matching iOS
    // Android library only provides these constants, so we map them to the closest equivalent
    return when (result) {
      Flic2ScanCallback.RESULT_SUCCESS -> 0 // SUCCESS
      Flic2ScanCallback.RESULT_FAILED_ALREADY_RUNNING -> 1 // ALREADY_RUNNING
      Flic2ScanCallback.RESULT_FAILED_BLUETOOTH_OFF -> 2 // BLUETOOTH_NOT_ACTIVATED
      Flic2ScanCallback.RESULT_FAILED_SCAN_ERROR -> 3 // UNKNOWN
      Flic2ScanCallback.RESULT_FAILED_NO_NEW_BUTTONS_FOUND -> 4 // NO_PUBLIC_BUTTON_DISCOVERED
      Flic2ScanCallback.RESULT_FAILED_BUTTON_ALREADY_CONNECTED_TO_OTHER_DEVICE -> 5 // ALREADY_CONNECTED_TO_ANOTHER_DEVICE
      Flic2ScanCallback.RESULT_FAILED_CONNECT_TIMED_OUT -> 6 // CONNECTION_TIMEOUT
      Flic2ScanCallback.RESULT_FAILED_VERIFY_TIMED_OUT -> 7 // INVALID_VERIFIER
      Flic2ScanCallback.RESULT_SYSTEM_PAIRING_DIALOG_NOT_ACCEPTED -> 9 // BLE_PAIRING_FAILED_USER_CANCELED
      else -> 3 // UNKNOWN (for any unexpected codes)
    }
  }
}

