package com.example.tv

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TvUrlDialog(
  currentUrl: String,
  onNavigate: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var urlText by remember { mutableStateOf(currentUrl) }

  val presets = listOf(
    "CineJoy Home" to "https://cinejoy.to/",
    "CineJoy Movies" to "https://cinejoy.to/movies",
    "CineJoy Series" to "https://cinejoy.to/tv-shows"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .widthIn(max = 600.dp)
        .padding(16.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131824)),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF).copy(alpha = 0.5f))
      )
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Tv,
              contentDescription = null,
              tint = Color(0xFF00E5FF)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Open Web URL on TV",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.Gray
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = urlText,
          onValueChange = { urlText = it },
          label = { Text("Website Address", color = Color(0xFF90CAF9)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color(0xFF37474F)
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Quick TV Presets:",
          color = Color(0xFFB0BEC5),
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          presets.forEach { (name, presetUrl) ->
            Box(
              modifier = Modifier
                .background(Color(0xFF1E283D), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable {
                  urlText = presetUrl
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Text(
                text = name,
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF263238),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel")
          }

          Spacer(modifier = Modifier.width(12.dp))

          Button(
            onClick = {
              var target = urlText.trim()
              if (!target.startsWith("http://") && !target.startsWith("https://")) {
                target = "https://$target"
              }
              onNavigate(target)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF00E5FF),
              contentColor = Color(0xFF0A0E17)
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Go to URL", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
