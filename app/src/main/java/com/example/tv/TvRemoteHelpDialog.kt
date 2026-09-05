package com.example.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TvRemoteHelpDialog(
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .widthIn(max = 560.dp)
        .padding(16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131824)),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF).copy(alpha = 0.5f)))
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Google TV Remote Guide",
          color = Color.White,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "How to control CineJoy on your TV screen",
          color = Color(0xFF90CAF9),
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        RemoteKeyGuideItem(
          icon = Icons.Default.Navigation,
          keyName = "D-Pad Arrows (▲ ▼ ◄ ►)",
          description = "Glide virtual mouse cursor smoothly across the screen, or scroll page."
        )

        RemoteKeyGuideItem(
          icon = Icons.Default.AdsClick,
          keyName = "Center / OK Button",
          description = "Simulates mouse click on movies, play buttons, servers, and players."
        )

        RemoteKeyGuideItem(
          icon = Icons.Default.Menu,
          keyName = "Menu Button / TV Top Pill",
          description = "Toggles TV Controls: Mode Switch, Zoom Level, Reload, and URL Bar."
        )

        RemoteKeyGuideItem(
          icon = Icons.Default.ArrowBack,
          keyName = "Back Button",
          description = "Navigates back in browser history. Press twice at root to exit."
        )

        RemoteKeyGuideItem(
          icon = Icons.Default.PlayArrow,
          keyName = "Media Play/Pause",
          description = "Quick pause and resume on supported video stream players."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00E5FF),
            contentColor = Color(0xFF0A0E17)
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(0.5f)
        ) {
          Text("Got It", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RemoteKeyGuideItem(
  icon: ImageVector,
  keyName: String,
  description: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .background(Color(0xFF1E283D), CircleShape)
        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color(0xFF00E5FF),
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(modifier = Modifier.width(16.dp))

    Column {
      Text(
        text = keyName,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = description,
        color = Color(0xFFB0BEC5),
        fontSize = 12.sp,
        lineHeight = 16.sp
      )
    }
  }
}
