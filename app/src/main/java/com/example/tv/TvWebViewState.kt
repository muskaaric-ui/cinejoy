package com.example.tv

import android.view.View
import android.webkit.WebChromeClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TvWebViewState(
  initialUrl: String = "https://cinejoy.to/"
) {
  var currentUrl by mutableStateOf(initialUrl)
  var pageTitle by mutableStateOf("CineJoy TV")
  var isLoading by mutableStateOf(true)
  var progress by mutableFloatStateOf(0f)
  var canGoBack by mutableStateOf(false)
  var canGoForward by mutableStateOf(false)
  var zoomLevel by mutableIntStateOf(115) // Default 115% for optimal 10-foot TV viewing

  // HTML5 Fullscreen Video view
  var customVideoView by mutableStateOf<View?>(null)
  var customViewCallback: WebChromeClient.CustomViewCallback? = null

  val isVideoFullscreen: Boolean
    get() = customVideoView != null

  fun setZoom(level: Int) {
    zoomLevel = level
  }
}
