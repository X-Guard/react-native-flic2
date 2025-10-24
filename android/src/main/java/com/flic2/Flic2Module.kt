package com.flic2

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = Flic2Module.NAME)
class Flic2Module(reactContext: ReactApplicationContext) :
  NativeFlic2Spec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    val result = a * b

    val eventData = Arguments.createMap().apply {
      putDouble("a", a)
      putDouble("b", b)
      putDouble("result", result)
    }
    emitOnMultiply(eventData)

    return result
  }

  companion object {
    const val NAME = "Flic2"
  }
}
