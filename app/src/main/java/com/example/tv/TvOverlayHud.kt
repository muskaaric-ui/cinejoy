package com.example.tv

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TvOverlayHud(
  state: TvWebViewState,
  remoteController: TvRemoteController,
  webView: WebView?,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top sleek progress bar when page is loading
    if (state.isLoading) {
      LinearProgressIndicator(
        progress = { state.progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp),
        color = Color(0xFF00E5FF),
        trackColor = Color(0x3300E5FF)
      )
    }

    // Floating TV Controls trigger pill at top
    Box(
      modifier = Modifier
        .padding(top = 4.dp)
        .shadow(8.dp, RoundedCornerShape(20.dp))
        .background(Color(0xD90D1117), RoundedCornerShape(20.dp))
        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))
        .clickable {
          remoteController.showHud = !remoteController.showHud
        }
        .padding(horizontal = 14.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (remoteController.showHud) Icons.Default.Close else Icons.Default.Menu,
          contentDescription = "Toggle TV Menu",
          tint = Color(0xFF00E5FF),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (remoteController.showHud) "Hide Menu" else "TV Remote Menu",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Mode indicator pill inside top trigger
        Box(
          modifier = Modifier
            .background(
              if (remoteController.mode == RemoteMode.CURSOR) Color(0xFF00E5FF).copy(alpha = 0.25f)
              else Color(0xFFFF9100).copy(alpha = 0.25f),
              RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = remoteController.mode.title,
            color = if (remoteController.mode == RemoteMode.CURSOR) Color(0xFF00E5FF) else Color(0xFFFF9100),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Expanded TV Quick Controls bar
    AnimatedVisibility(
      visible = remoteController.showHud,
      enter = slideInVertically(initialOffsetY = { -it }),
      exit = slideOutVertically(targetOffsetY = { -it })
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20F1626)),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF).copy(alpha = 0.35f))
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left: Web Navigation Controls
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            TvHudActionButton(
              icon = Icons.Default.Home,
              label = "Home",
              onClick = {
                webView?.loadUrl("https://cinejoy.to/")
                remoteController.showModeNotification("Returning to CineJoy Home")
              }
            )

            TvHudActionButton(
              icon = Icons.Default.ArrowBack,
              label = "Back",
              enabled = state.canGoBack,
              onClick = {
                if (webView?.canGoBack() == true) {
                  webView.goBack()
                }
              }
            )

            TvHudActionButton(
              icon = Icons.Default.ArrowForward,
              label = "Forward",
              enabled = state.canGoForward,
              onClick = {
                if (webView?.canGoForward() == true) {
                  webView.goForward()
                }
              }
            )

            TvHudActionButton(
              icon = Icons.Default.Refresh,
              label = "Reload",
              onClick = {
                webView?.reload()
                remoteController.showModeNotification("Reloading CineJoy...")
              }
            )
          }

          // Center: URL indicator and custom URL opener
          Row(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 12.dp)
              .background(Color(0xFF1B2438), RoundedCornerShape(12.dp))
              .border(1.dp, Color(0xFF263238), RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                remoteController.showUrlDialog = true
              }
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = state.currentUrl,
              color = Color(0xFFB0BEC5),
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit URL",
              tint = Color(0xFF00E5FF),
              modifier = Modifier.size(16.dp)
            )
          }

          // Right: Remote & TV View Settings
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Mode Toggle (Virtual Cursor vs D-Pad Scroll)
            TvHudActionButton(
              icon = if (remoteController.mode == RemoteMode.CURSOR) Icons.Default.Mouse else Icons.Default.SwapVert,
              label = if (remoteController.mode == RemoteMode.CURSOR) "Cursor" else "Scroll",
              isActive = true,
              activeColor = if (remoteController.mode == RemoteMode.CURSOR) Color(0xFF00E5FF) else Color(0xFFFF9100),
              onClick = {
                remoteController.toggleMode()
              }
            )

            // Speed toggle
            TvHudActionButton(
              icon = Icons.Default.Speed,
              label = remoteController.cursorSpeed.label,
              onClick = {
                remoteController.cycleSpeed()
              }
            )

            // TV Zoom toggle
            TvHudActionButton(
              icon = Icons.Default.ZoomIn,
              label = "${state.zoomLevel}%",
              onClick = {
                val nextZoom = when (state.zoomLevel) {
                  100 -> 115
                  115 -> 125
                  125 -> 140
                  else -> 100
                }
                state.setZoom(nextZoom)
                remoteController.showModeNotification("TV Display Zoom: $nextZoom%")
              }
            )

            // Remote Help Guide
            TvHudActionButton(
              icon = Icons.Default.HelpOutline,
              label = "Guide",
              onClick = {
                remoteController.showHelpDialog = true
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TvHudActionButton(
  icon: ImageVector,
  label: String,
  enabled: Boolean = true,
  isActive: Boolean = false,
  activeColor: Color = Color(0xFF00E5FF),
  onClick: () -> Unit
) {
  val contentColor = if (!enabled) Color(0xFF455A64)
  else if (isActive) activeColor
  else Color.White

  val bgColor = if (isActive) activeColor.copy(alpha = 0.15f) else Color(0xFF192233)
  val borderColor = if (isActive) activeColor.copy(alpha = 0.4f) else Color.Transparent

  Box(
    modifier = Modifier
      .shadow(2.dp, RoundedCornerShape(10.dp))
      .background(bgColor, RoundedCornerShape(10.dp))
      .border(1.dp, borderColor, RoundedCornerShape(10.dp))
      .clip(RoundedCornerShape(10.dp))
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        color = contentColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
