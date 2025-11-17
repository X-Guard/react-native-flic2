package nl.xguard.flic2

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import io.flic.flic2libandroid.BatteryLevel
import io.flic.flic2libandroid.Flic2Button
import io.flic.flic2libandroid.Flic2ButtonListener

class Flic2ButtonEventListener(
    private val emitEvent: (WritableMap) -> Unit
) : Flic2ButtonListener() {

    // Map Android library's onConnect() to emit "connected" (matches iOS)
    override fun onConnect(button: Flic2Button) {
        emitEvent(createButtonEvent(button, "connected"))
    }

    // Map Android library's onReady() to emit "ready" (matches iOS)
    override fun onReady(button: Flic2Button, timestamp: Long) {
        emitEvent(createButtonEvent(button, "ready"))
    }

    // Map Android library's onDisconnect() to emit "disconnected" (matches iOS)
    override fun onDisconnect(button: Flic2Button) {
        emitEvent(createButtonEvent(button, "disconnected"))
    }

    // Map Android library's onFailure() to emit "connectionFailed" (matches iOS)
    override fun onFailure(button: Flic2Button, errorCode: Int, subCode: Int) {
        emitEvent(createButtonEvent(button, "connectionFailed").apply {
            putMap("error", Arguments.createMap().apply {
                putInt("code", errorCode)
                putInt("subCode", subCode)
                putString("message", "Connection failed: code=$errorCode, subCode=$subCode")
            })
        })
    }

    override fun onButtonUpOrDown(
        button: Flic2Button,
        wasQueued: Boolean,
        lastQueued: Boolean,
        timestamp: Long,
        isUp: Boolean,
        isDown: Boolean
    ) {
        val event = if (isDown) "buttonDown" else "buttonUp"
        emitEvent(createButtonEvent(button, event).apply {
            putBoolean("queued", wasQueued)
            putDouble("age", System.currentTimeMillis() - timestamp.toDouble())
        })
    }

    // Android library calls ALL applicable callback methods, causing duplicate events.
    // Only onButtonSingleOrDoubleClickOrHold should emit events to match iOS behavior.
    override fun onButtonClickOrHold(
        button: Flic2Button,
        wasQueued: Boolean,
        lastQueued: Boolean,
        timestamp: Long,
        isClick: Boolean,
        isHold: Boolean
    ) {
        // Intentionally empty - events are handled by onButtonSingleOrDoubleClickOrHold
    }

    // Android library calls ALL applicable callback methods, causing duplicate events.
    // Only onButtonSingleOrDoubleClickOrHold should emit events to match iOS behavior.
    override fun onButtonSingleOrDoubleClick(
        button: Flic2Button,
        wasQueued: Boolean,
        lastQueued: Boolean,
        timestamp: Long,
        isSingleClick: Boolean,
        isDoubleClick: Boolean
    ) {
        // Intentionally empty - events are handled by onButtonSingleOrDoubleClickOrHold
    }

    override fun onButtonSingleOrDoubleClickOrHold(
        button: Flic2Button,
        wasQueued: Boolean,
        lastQueued: Boolean,
        timestamp: Long,
        isSingleClick: Boolean,
        isDoubleClick: Boolean,
        isHold: Boolean
    ) {
        val event = when {
            isSingleClick -> "click"
            isDoubleClick -> "doubleClick"
            isHold -> "hold"
            else -> "unknown"
        }
        emitEvent(createButtonEvent(button, event).apply {
            putBoolean("queued", wasQueued)
            putDouble("age", System.currentTimeMillis() - timestamp.toDouble())
        })
    }

    // Map Android library's onUnpaired() to emit "unpaired" (matches iOS)
    override fun onUnpaired(button: Flic2Button) {
        emitEvent(createButtonEvent(button, "unpaired"))
    }

    // Map Android library's onBatteryLevelUpdated() to emit "batteryUpdate" (matches iOS)
    override fun onBatteryLevelUpdated(button: Flic2Button, level: BatteryLevel) {
        emitEvent(createButtonEvent(button, "batteryUpdate").apply {
            putDouble("voltage", level.voltage.toDouble())
        })
    }

    // Map Android library's onNameUpdated() to emit "nicknameUpdate" (matches iOS)
    override fun onNameUpdated(button: Flic2Button, newName: String) {
        emitEvent(createButtonEvent(button, "nicknameUpdate").apply {
            putString("nickname", newName)
        })
    }

    private fun createButtonEvent(button: Flic2Button, event: String): WritableMap {
        return Arguments.createMap().apply {
            putString("uuid", button.uuid)
            putString("event", event)
            putMap("button", Flic2Converter.buttonToMap(button))
        }
    }
}

