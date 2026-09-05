package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tv.RemoteMode
import com.example.tv.TvCursorOverlay
import com.example.tv.TvModeNotificationBadge
import com.example.tv.TvOverlayHud
import com.example.tv.TvRemoteController
import com.example.tv.TvRemoteHelpDialog
import com.example.tv.TvUrlDialog
import com.example.tv.TvWebViewComponent
import com.example.tv.TvWebViewState
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private var webViewRef: WebView? = null
  private var remoteController: TvRemoteController? = null
  private var webViewState: TvWebViewState? = null
  private var lastBackPressTime = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    // Keep Google TV screen awake while viewing
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    setContent {
      MyApplicationTheme {
        CineJoyTvApp(
          onControllerReady = { controller, state, webView ->
            remoteController = controller
            webViewState = state
            webViewRef = webView
          }
        )
      }
    }
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    val controller = remoteController
    val webView = webViewRef
    val state = webViewState

    if (event.action == KeyEvent.ACTION_DOWN) {
      val isHeld = event.repeatCount > 0
      controller?.onDirectionKeyHeld(isHeld)

      when (event.keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> {
          if (controller != null) {
            if (controller.mode == RemoteMode.CURSOR) {
              controller.moveCursor(0f, -1f)
            } else {
              controller.scrollPage(-180)
            }
            return true
          }
        }
        KeyEvent.KEYCODE_DPAD_DOWN -> {
          if (controller != null) {
            if (controller.mode == RemoteMode.CURSOR) {
              controller.moveCursor(0f, 1f)
            } else {
              controller.scrollPage(180)
            }
            return true
          }
        }
        KeyEvent.KEYCODE_DPAD_LEFT -> {
          if (controller != null) {
            if (controller.mode == RemoteMode.CURSOR) {
              controller.moveCursor(-1f, 0f)
            } else {
              controller.scrollPage(-60)
            }
            return true
          }
        }
        KeyEvent.KEYCODE_DPAD_RIGHT -> {
          if (controller != null) {
            if (controller.mode == RemoteMode.CURSOR) {
              controller.moveCursor(1f, 0f)
            } else {
              controller.scrollPage(60)
            }
            return true
          }
        }
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
          if (controller != null) {
            if (controller.mode == RemoteMode.CURSOR) {
              return controller.performClick()
            } else {
              // In scroll mode, delegate enter or simulate click at cursor
              return controller.performClick()
            }
          }
        }
        KeyEvent.KEYCODE_MENU,
        KeyEvent.KEYCODE_SETTINGS,
        KeyEvent.KEYCODE_INFO -> {
          if (controller != null) {
            controller.showHud = !controller.showHud
            return true
          }
        }
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
          // Send simulated Space key to toggle play/pause on web video
          webView?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
          webView?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE))
          return true
        }
        KeyEvent.KEYCODE_BACK -> {
          // 1. If Fullscreen video is playing, close fullscreen
          if (state?.isVideoFullscreen == true) {
            state.customVideoView = null
            state.customViewCallback?.onCustomViewHidden()
            state.customViewCallback = null
            return true
          }
          // 2. If Dialogs or HUD are open, dismiss them
          if (controller?.showHelpDialog == true) {
            controller.showHelpDialog = false
            return true
          }
          if (controller?.showUrlDialog == true) {
            controller.showUrlDialog = false
            return true
          }
          if (controller?.showHud == true) {
            controller.showHud = false
            return true
          }
          // 3. If WebView can go back, navigate back
          if (webView?.canGoBack() == true) {
            webView.goBack()
            return true
          }
          // 4. Double tap back to exit application
          val currentTime = System.currentTimeMillis()
          if (currentTime - lastBackPressTime < 2000) {
            finish()
          } else {
            lastBackPressTime = currentTime
            Toast.makeText(this, "Press Back again to exit CineJoy TV", Toast.LENGTH_SHORT).show()
          }
          return true
        }
      }
    } else if (event.action == KeyEvent.ACTION_UP) {
      controller?.onDirectionKeyHeld(false)
    }

    return super.dispatchKeyEvent(event)
  }

  override fun onDestroy() {
    super.onDestroy()
    webViewRef?.destroy()
    webViewRef = null
  }
}

@Composable
fun CineJoyTvApp(
  onControllerReady: (TvRemoteController, TvWebViewState, WebView) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var internalWebView by remember { mutableStateOf<WebView?>(null) }
  val webViewState = remember { TvWebViewState(initialUrl = "https://cinejoy.to/") }
  val remoteController = remember {
    TvRemoteController(coroutineScope) { internalWebView }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // Keep screen size updated in remote controller for pointer clamping
    DisposableEffect(maxWidth, maxHeight) {
      remoteController.setScreenSize(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
      onDispose { }
    }

    if (webViewState.isVideoFullscreen && webViewState.customVideoView != null) {
      // Fullscreen HTML5 Video Takeover for Google TV
      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
          FrameLayout(it).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewState.customVideoView?.let { customView ->
              (customView.parent as? ViewGroup)?.removeView(customView)
              addView(customView)
            }
          }
        }
      )
    } else {
      // Primary TV Web View
      TvWebViewComponent(
        state = webViewState,
        onWebViewCreated = { webView ->
          internalWebView = webView
          onControllerReady(remoteController, webViewState, webView)
        }
      )

      // Virtual Mouse Cursor Overlay
      TvCursorOverlay(
        remoteController = remoteController,
        modifier = Modifier.fillMaxSize()
      )

      // Quick TV Actions & Navigation HUD
      TvOverlayHud(
        state = webViewState,
        remoteController = remoteController,
        webView = internalWebView
      )

      // Temporary Mode / Action Notification Badge
      TvModeNotificationBadge(
        message = remoteController.modeNotification,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 36.dp)
      )
    }

    // Remote Help Guide Modal
    if (remoteController.showHelpDialog) {
      TvRemoteHelpDialog(
        onDismiss = { remoteController.showHelpDialog = false }
      )
    }

    // Custom URL Opener Modal
    if (remoteController.showUrlDialog) {
      TvUrlDialog(
        currentUrl = webViewState.currentUrl,
        onNavigate = { targetUrl ->
          internalWebView?.loadUrl(targetUrl)
        },
        onDismiss = { remoteController.showUrlDialog = false }
      )
    }
  }
}

// Retained for test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}
