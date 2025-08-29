package com.example.composespeedtest.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.composespeedtest.ui.theme.DarkGradient
import com.example.composespeedtest.ui.theme.Teal200
import androidx.compose.material.Surface

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f)
    val scale by animateFloatAsState(targetValue = if (visible) 1f else 0.85f, animationSpec = tween(800))

    LaunchedEffect(Unit) {
        visible = true
        delay(1600)
        onFinished()
    }

    Surface(color = MaterialTheme.colors.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Speed Tester",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(alpha).scale(scale),
                    color = Teal200
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preparing environment…",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.alpha(alpha * 0.9f)
                )
            }
        }
    }
}
