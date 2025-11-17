package nl.xguard.flic2

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import io.flic.flic2libandroid.Flic2Button

object Flic2Converter {

    fun buttonToMap(button: Flic2Button): WritableMap {
        return Arguments.createMap().apply {
            putString("uuid", button.uuid)
            putString("identifier", button.uuid) // Android uses UUID as identifier
            putString("name", button.getName() ?: "")
            putString("nickname", button.getName() ?: "")
            putString("bluetoothAddress", button.getBdAddr() ?: "")
            putString("serialNumber", button.getSerialNumber() ?: "")

            // Use connection state constants instead of enum
            val connState = button.getConnectionState()
            putInt("state", connectionStateToInt(connState))
            putString("stateName", connectionStateToString(connState))

            // iOS-only: Return defaults for trigger/latency mode (not supported in Android v1.1.0+)
            putInt("triggerMode", 0)
            putString("triggerModeName", "")
            putInt("latencyMode", 0)
            putString("latencyModeName", "")

            putInt("pressCount", button.getPressCount())
            putInt("firmwareRevision", button.getFirmwareVersion())

            // Check if ready by comparing connection state
            val isReady = connState == Flic2Button.CONNECTION_STATE_CONNECTED_READY
            putBoolean("isReady", isReady)

            // Get battery level from BatteryLevel object
            val batteryLevel = button.getLastKnownBatteryLevel()
            putDouble("batteryVoltage", batteryLevel?.voltage?.toDouble() ?: 0.0)

            putBoolean("isUnpaired", button.isUnpaired())
        }
    }

    fun buttonsToArray(buttons: List<Flic2Button>): WritableArray {
        return Arguments.createArray().apply {
            buttons.forEach { button ->
                pushMap(buttonToMap(button))
            }
        }
    }

    private fun connectionStateToInt(state: Int): Int {
        return when (state) {
            Flic2Button.CONNECTION_STATE_DISCONNECTED -> 0
            Flic2Button.CONNECTION_STATE_CONNECTING -> 1
            Flic2Button.CONNECTION_STATE_CONNECTED_STARTING -> 2
            Flic2Button.CONNECTION_STATE_CONNECTED_READY -> 3
            else -> 0
        }
    }

    private fun connectionStateToString(state: Int): String {
        return when (state) {
            Flic2Button.CONNECTION_STATE_DISCONNECTED -> "disconnected"
            Flic2Button.CONNECTION_STATE_CONNECTING -> "connecting"
            Flic2Button.CONNECTION_STATE_CONNECTED_STARTING -> "connected"
            Flic2Button.CONNECTION_STATE_CONNECTED_READY -> "connected"
            else -> "disconnected"
        }
    }

    // Trigger mode and latency mode are iOS-only features (removed from Android library v1.1.0+)

    fun scanResultToString(result: Int): String {
        return when (result) {
            0 -> "success"
            1 -> "alreadyRunning"
            2 -> "bluetoothNotActivated"
            3 -> "unknown"
            4 -> "noPublicButtonDiscovered"
            5 -> "alreadyConnectedToAnotherDevice"
            6 -> "connectionTimeout"
            7 -> "invalidVerifier"
            8 -> "blePairingFailedPreviousPairingAlreadyExisting"
            9 -> "blePairingFailedUserCanceled"
            10 -> "blePairingFailedUnknownReason"
            11 -> "appCredentialsDontMatch"
            12 -> "userCanceled"
            13 -> "invalidBluetoothAddress"
            14 -> "genuineCheckFailed"
            15 -> "tooManyApps"
            16 -> "couldNotSetBluetoothNotify"
            17 -> "couldNotDiscoverBluetoothServices"
            18 -> "buttonDisconnectedDuringVerification"
            19 -> "failedToEstablish"
            20 -> "connectionLimitReached"
            21 -> "notInPublicMode"
            else -> "unknown"
        }
    }
}

