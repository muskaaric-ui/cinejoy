package com.example.tv

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

enum class RemoteMode(val title: String) {
  CURSOR("Virtual Pointer"),
  SCROLL("D-Pad Scroll")
}

enum class CursorSpeed(val multiplier: Float, val label: String) {
  NORMAL(1.0f, "1.0x"),
  FAST(1.6f, "1.6x"),
  ULTRA(2.2f, "2.2x")
}

class TvRemoteController(
  private val coroutineScope: CoroutineScope,
  var targetViewProvider: () -> View?
) {
  var mode by mutableStateOf(RemoteMode.CURSOR)
  var cursorSpeed by mutableStateOf(CursorSpeed.FAST)
  var isCursorVisible by mutableStateOf(true)
  var isClicking by mutableStateOf(false)
  var showHud by mutableStateOf(false)
  var showHelpDialog by mutableStateOf(false)
  var showUrlDialog by mutableStateOf(false)
  var modeNotification by mutableStateOf<String?>(null)

  // Cursor coordinates in screen pixels
  var cursorX by mutableFloatStateOf(960f)
  var cursorY by mutableFloatStateOf(540f)

  // Screen bounds
  var screenWidth by mutableFloatStateOf(1920f)
  var screenHeight by mutableFloatStateOf(1080f)

  private var hideCursorJob: Job? = null
  private var notificationJob: Job? = null
  private var movementMultiplier = 1.0f
  private var isHoldingDirection = false

  fun setScreenSize(width: Float, height: Float) {
    if (width > 0 && height > 0) {
      screenWidth = width
      screenHeight = height
      // Center cursor if at initial position
      if (cursorX == 960f && cursorY == 540f) {
        cursorX = width / 2f
        cursorY = height / 2f
      }
    }
  }

  fun toggleMode() {
    mode = if (mode == RemoteMode.CURSOR) RemoteMode.SCROLL else RemoteMode.CURSOR
    showModeNotification(if (mode == RemoteMode.CURSOR) "Virtual Cursor Mode (D-Pad moves pointer, OK clicks)" else "Scroll Mode (D-Pad scrolls page)")
    resetCursorInactivityTimer()
  }

  fun cycleSpeed() {
    cursorSpeed = when (cursorSpeed) {
      CursorSpeed.NORMAL -> CursorSpeed.FAST
      CursorSpeed.FAST -> CursorSpeed.ULTRA
      CursorSpeed.ULTRA -> CursorSpeed.NORMAL
    }
    showModeNotification("Cursor Speed: ${cursorSpeed.label}")
  }

  fun showModeNotification(message: String) {
    modeNotification = message
    notificationJob?.cancel()
    notificationJob = coroutineScope.launch {
      delay(2500)
      modeNotification = null
    }
  }

  fun resetCursorInactivityTimer() {
    isCursorVisible = true
    hideCursorJob?.cancel()
    hideCursorJob = coroutineScope.launch {
      delay(6000)
      // Auto-fade cursor after 6 seconds of no remote input
      if (mode == RemoteMode.CURSOR) {
        isCursorVisible = false
      }
    }
  }

  fun onDirectionKeyHeld(isHeld: Boolean) {
    isHoldingDirection = isHeld
    movementMultiplier = if (isHeld) 1.5f else 1.0f
  }

  fun moveCursor(dx: Float, dy: Float): Boolean {
    resetCursorInactivityTimer()
    val baseStep = 22f * cursorSpeed.multiplier * movementMultiplier
    val nextX = cursorX + (dx * baseStep)
    val nextY = cursorY + (dy * baseStep)

    cursorX = max(10f, min(screenWidth - 10f, nextX))
    cursorY = max(10f, min(screenHeight - 10f, nextY))

    // If cursor reaches top or bottom screen boundary, auto-scroll the page!
    if (cursorY <= 25f && dy < 0) {
      scrollPage(-120)
    } else if (cursorY >= screenHeight - 25f && dy > 0) {
      scrollPage(120)
    }

    return true
  }

  fun scrollPage(amountY: Int) {
    val view = targetViewProvider() ?: return
    view.scrollBy(0, amountY)
  }

  fun performClick(): Boolean {
    resetCursorInactivityTimer()
    val view = targetViewProvider() ?: return false
    isClicking = true

    val downTime = SystemClock.uptimeMillis()
    val eventTime = SystemClock.uptimeMillis()

    val downEvent = MotionEvent.obtain(
      downTime,
      eventTime,
      MotionEvent.ACTION_DOWN,
      cursorX,
      cursorY,
      0
    )
    view.dispatchTouchEvent(downEvent)
    downEvent.recycle()

    coroutineScope.launch {
      delay(80)
      val upTime = SystemClock.uptimeMillis()
      val upEvent = MotionEvent.obtain(
        downTime,
        upTime,
        MotionEvent.ACTION_UP,
        cursorX,
        cursorY,
        0
      )
      view.dispatchTouchEvent(upEvent)
      upEvent.recycle()
      delay(120)
      isClicking = false
    }

    return true
  }
}
