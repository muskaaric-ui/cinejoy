package com.example.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TvCursorOverlay(
  remoteController: TvRemoteController,
  modifier: Modifier = Modifier
) {
  if (remoteController.mode != RemoteMode.CURSOR) return

  AnimatedVisibility(
    visible = remoteController.isCursorVisible,
    enter = fadeIn(tween(150)),
    exit = fadeOut(tween(300)),
    modifier = modifier
  ) {
    val clickScale by animateFloatAsState(
      targetValue = if (remoteController.isClicking) 0.8f else 1.0f,
      animationSpec = tween(durationMillis = 80),
      label = "clickScale"
    )

    Box(
      modifier = Modifier
        .offset {
          IntOffset(
            x = (remoteController.cursorX - 16.dp.toPx()).roundToInt(),
            y = (remoteController.cursorY - 16.dp.toPx()).roundToInt()
          )
        }
        .size(36.dp)
        .scale(clickScale),
      contentAlignment = Alignment.Center
    ) {
      // High performance solid geometry (Zero gradients, zero GPU shader recalculation)
      Canvas(modifier = Modifier.size(32.dp)) {
        // Crisp high-contrast outer ring with solid color
        drawCircle(
          color = Color(0xFF00E5FF),
          radius = 11.dp.toPx(),
          style = Stroke(width = 2.5.dp.toPx())
        )

        // Center solid target point
        drawCircle(
          color = Color.White,
          radius = 3.5.dp.toPx()
        )
      }
    }
  }
}

@Composable
fun TvModeNotificationBadge(
  message: String?,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = message != null,
    enter = fadeIn(tween(150)),
    exit = fadeOut(tween(250)),
    modifier = modifier
  ) {
    if (message != null) {
      Box(
        modifier = Modifier
          .shadow(12.dp, RoundedCornerShape(24.dp))
          .background(Color(0xE610141D), RoundedCornerShape(24.dp))
          .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
          .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = message,
          color = Color.White,
          fontSize = 15.sp
        )
      }
    }
  }
}
