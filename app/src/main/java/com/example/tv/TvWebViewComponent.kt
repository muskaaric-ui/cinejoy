package com.example.tv

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TvWebViewComponent(
  state: TvWebViewState,
  onWebViewCreated: (WebView) -> Unit,
  modifier: Modifier = Modifier
) {
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        isFocusable = true
        isFocusableInTouchMode = true

        // TV-ready WebSettings
        settings.apply {
          javaScriptEnabled = true
          javaScriptCanOpenWindowsAutomatically = true
          domStorageEnabled = true
          databaseEnabled = true
          mediaPlaybackRequiresUserGesture = false
          loadWithOverviewMode = true
          useWideViewPort = true
          allowContentAccess = true
          allowFileAccess = true
          mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
          cacheMode = WebSettings.LOAD_DEFAULT
          textZoom = state.zoomLevel

          // High-performance Desktop/TV User-Agent for CineJoy widescreen player
          userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)

        webChromeClient = object : WebChromeClient() {
          override fun onProgressChanged(view: WebView?, newProgress: Int) {
            state.progress = newProgress / 100f
            state.isLoading = newProgress < 100
          }

          override fun onReceivedTitle(view: WebView?, title: String?) {
            if (!title.isNullOrBlank()) {
              state.pageTitle = title
            }
          }

          override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            if (view != null) {
              state.customVideoView = view
              state.customViewCallback = callback
            }
          }

          override fun onHideCustomView() {
            state.customVideoView = null
            state.customViewCallback?.onCustomViewHidden()
            state.customViewCallback = null
          }
        }

        webViewClient = object : WebViewClient() {
          override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            state.isLoading = true
            url?.let { state.currentUrl = it }
          }

          override fun onPageFinished(view: WebView?, url: String?) {
            state.isLoading = false
            url?.let { state.currentUrl = it }
            state.canGoBack = canGoBack()
            state.canGoForward = canGoForward()

            // Inject TV remote friendly CSS styles: distinct focus ring for D-pad navigation
            val tvFocusCss = """
              javascript:(function() {
                var style = document.getElementById('tv-remote-style');
                if (!style) {
                  style = document.createElement('style');
                  style.id = 'tv-remote-style';
                  style.innerHTML = 'a:focus, button:focus, input:focus, [tabindex]:focus { outline: 3px solid #00E5FF !important; outline-offset: 2px !important; box-shadow: 0 0 12px rgba(0,229,255,0.7) !important; }';
                  document.head.appendChild(style);
                }
              })()
            """.trimIndent()
            evaluateJavascript(tvFocusCss, null)
          }

          override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val nextUrl = request?.url?.toString() ?: return false
            // Suppress non-web intents or ad scheme redirects
            if (nextUrl.startsWith("http://") || nextUrl.startsWith("https://")) {
              return false // Let WebView handle normal navigations
            }
            return true
          }

          override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            // Proceed so media CDN streams don't fail silently on TV
            handler?.proceed()
          }
        }

        loadUrl(state.currentUrl)
        onWebViewCreated(this)
      }
    },
    update = { webView ->
      // Update textZoom if zoom level changed
      if (webView.settings.textZoom != state.zoomLevel) {
        webView.settings.textZoom = state.zoomLevel
      }
    }
  )
}
