package com.example.composespeedtest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composespeedtest.ui.theme.DarkGradient
import com.example.composespeedtest.ui.theme.Teal200

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val page = remember { mutableStateOf(0) }
    val pages = listOf(
        "Welcome to Speed Tester",
        "Measure download, upload, and latency",
        "View results and track performance"
    )

    Box(modifier = Modifier.fillMaxSize().background(DarkGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = pages[page.value], style = MaterialTheme.typography.h6, color = Color.White)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.forEachIndexed { index, _ ->
                    val active = index == page.value
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) Teal200 else Color.White.copy(alpha = 0.3f))
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFinished) { Text("Skip") }
                Button(
                    onClick = {
                        if (page.value < pages.lastIndex) page.value += 1 else onFinished()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Teal200, contentColor = Color.Black)
                ) {
                    Text(if (page.value < pages.lastIndex) "Next" else "Get started")
                }
            }
        }
    }
}
