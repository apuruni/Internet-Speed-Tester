package com.example.composespeedtest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composespeedtest.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SpeedTestScreen() {
    val viewModel: SpeedTestViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context=LocalContext.current
    val prefs = remember { com.example.composespeedtest.data.Prefs(context.applicationContext) }
    var darkMode by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        prefs.darkTheme.collect { darkMode = it }
    }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (selectedTab) {
                        0 -> "Speed Test"
                        1 -> "Status"
                        else -> "Settings"
                    }
                    Text(title, color = Color.White)
                },
                backgroundColor = DarkColor,
                elevation = 0.dp
            )
        },
        backgroundColor = DarkColor,
        bottomBar = {
            BottomNavigation(backgroundColor = DarkColor2, contentColor = Color.White) {
                BottomNavigationItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selectedContentColor = Teal200,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Stats") },
                    label = { Text("Stats") },
                    selectedContentColor = Teal200,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selectedContentColor = Teal200,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    ) { pad ->
        when (selectedTab) {
            0 -> SpeedTestScreenContent(
                uiState = uiState,
                onStartTest = { viewModel.startSpeedTest() },
                onResetTest = { viewModel.resetTest() },
                modifier = Modifier.padding(pad),
                useDarkGradient = darkMode
            )
            1 -> StatusScreenContent(
                uiState = uiState,
                modifier = Modifier.padding(pad)
            )
            else -> SettingsScreenContent(
                darkMode = darkMode,
                onDarkModeChange = { enabled ->
                    darkMode = enabled
                    kotlinx.coroutines.GlobalScope.launch { prefs.setDarkTheme(enabled) }
                },
                modifier = Modifier.padding(pad)
            )
        }
    }
}

@Composable
fun SpeedTestScreenContent(
    uiState: SpeedTestUiState,
    onStartTest: () -> Unit,
    onResetTest: () -> Unit,
    modifier: Modifier = Modifier,
    useDarkGradient: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(if (useDarkGradient) DarkGradient else IndigoGradient)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Gauge Card
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFF1A1F3A),
            shape = RoundedCornerShape(24.dp),
            elevation = 8.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(12.dp)
            ) {
                val maxSpeed = 200f
                val currentSpeed = when {
                    uiState.isTestRunning && uiState.instantSamples.isNotEmpty() -> uiState.instantSamples.last()
                    uiState.hasResults -> uiState.downloadSpeed
                    else -> 0f
                }
                SpeedometerGauge(
                    currentSpeedMbps = currentSpeed,
                    maxSpeedMbps = maxSpeed,
                    progress = uiState.progress,
                    isRunning = uiState.isTestRunning
                )
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val density = LocalDensity.current
                    val maxW = maxWidth
                    val dynamicSize = with(density) { (maxW * 0.18f).toSp() }
                    val valueFont = when {
                        dynamicSize > 44.sp -> 44.sp
                        dynamicSize < 28.sp -> 28.sp
                        else -> dynamicSize
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.hasResults) "Download" else if (uiState.isTestRunning) "Measuring…" else "Ready",
                            style = MaterialTheme.typography.caption,
                            color = LightColor
                        )
                        // Number moved outside the gauge
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = uiState.currentTest,
                            style = MaterialTheme.typography.caption,
                            color = LightColor2
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!uiState.isTestRunning && !uiState.hasResults) {
                        PrimaryButton(text = "Start", onClick = onStartTest)
                    } else if (uiState.hasResults) {
                        PrimaryButton(text = "Run again", onClick = onResetTest)
                    }
                }
            }
        }

        // Current speed below gauge (centered) with enhanced styling
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFF1A1F3A).copy(alpha = 0.8f),
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentSpeed = when {
                    uiState.isTestRunning && uiState.instantSamples.isNotEmpty() -> uiState.instantSamples.last()
                    uiState.hasResults -> uiState.downloadSpeed
                    else -> 0f
                }
                Text(
                    text = "%.1f".format(currentSpeed),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Mbps",
                    style = MaterialTheme.typography.h6,
                    color = Teal200,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Min / Avg / Max below gauge for cleaner alignment
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFF1A1F3A).copy(alpha = 0.6f),
            shape = RoundedCornerShape(16.dp),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedGaugeStat(label = "MIN", value = uiState.minSpeed, color = Color(0xFF4CAF50))
                EnhancedGaugeStat(label = "AVG", value = uiState.avgSpeed, color = Teal200)
                EnhancedGaugeStat(label = "MAX", value = uiState.maxSpeed, color = Color(0xFFFF9800))
            }
        }

        // Sparkline
        if (uiState.instantSamples.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                backgroundColor = Color(0xFF1A1F3A),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Speed Chart",
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()) {
                        EnhancedSpeedSparkline(samples = uiState.instantSamples)
                    }
                }
            }
        }

        // Stats Row 1
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnhancedStatCard(
                title = "Download", 
                value = if (uiState.hasResults) "%.1f Mbps".format(uiState.downloadSpeed) else "--", 
                icon = "⬇️",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            EnhancedStatCard(
                title = "Upload", 
                value = if (uiState.hasResults) "%.1f Mbps".format(uiState.uploadSpeed) else "--", 
                icon = "⬆️",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        // Stats Row 2
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnhancedStatCard(
                title = "Ping", 
                value = if (uiState.hasResults) "${uiState.ping} ms" else "--", 
                icon = "📡",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            EnhancedStatCard(
                title = "Jitter", 
                value = if (uiState.hasResults) "%.1f ms".format(uiState.jitter) else "--", 
                icon = "📊",
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        // Footer row with enhanced styling
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFF1A1F3A).copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📉", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Packet Loss: ${if (uiState.hasResults) "%.1f%%".format(uiState.packetLoss) else "--"}",
                        style = MaterialTheme.typography.body2,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                if (uiState.hasResults && uiState.networkType.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📶", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = uiState.networkType,
                            style = MaterialTheme.typography.body2,
                            color = Teal200,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (uiState.isTestRunning) {
            TestProgressCard(uiState)
        }

        if (uiState.errorMessage != null) {
            ErrorMessage(uiState.errorMessage!!)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatusScreenContent(
    uiState: SpeedTestUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGradient)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Card(
            backgroundColor = Color(0xFF1A1F3A),
            shape = RoundedCornerShape(20.dp),
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("📊", fontSize = 24.sp)
                Text(
                    "Network Status", 
                    style = MaterialTheme.typography.h5, 
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (!uiState.hasResults) {
            Card(
                backgroundColor = Color(0xFF1A1F3A).copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ℹ️", fontSize = 20.sp)
                    Text(
                        text = "No recent measurement. Start a test to see results here.",
                        style = MaterialTheme.typography.body2,
                        color = LightColor2,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Stats Grid
        if (uiState.hasResults) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    EnhancedStatCard(
                        title = "Download", 
                        value = "%.1f Mbps".format(uiState.downloadSpeed), 
                        icon = "⬇️",
                        color = Color(0xFF4CAF50)
                    )
                }
                item {
                    EnhancedStatCard(
                        title = "Upload", 
                        value = "%.1f Mbps".format(uiState.uploadSpeed), 
                        icon = "⬆️",
                        color = Color(0xFF2196F3)
                    )
                }
                item {
                    EnhancedStatCard(
                        title = "Latency", 
                        value = "${uiState.ping} ms", 
                        icon = "📡",
                        color = Color(0xFFFF9800)
                    )
                }
                item {
                    EnhancedStatCard(
                        title = "Jitter", 
                        value = "%.1f ms".format(uiState.jitter), 
                        icon = "📊",
                        color = Color(0xFF9C27B0)
                    )
                }
            }
            
            // Packet Loss Card
            Card(
                backgroundColor = Color(0xFF1A1F3A),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📉", fontSize = 20.sp)
                    Text(
                        "Packet Loss: %.1f%%".format(uiState.packetLoss),
                        style = MaterialTheme.typography.h6,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Speed Chart
        if (uiState.instantSamples.isNotEmpty()) {
            Card(
                backgroundColor = Color(0xFF1A1F3A),
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📈", fontSize = 20.sp)
                        Text(
                            "Speed Chart",
                            style = MaterialTheme.typography.h6,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()) {
                        EnhancedSpeedSparkline(samples = uiState.instantSamples)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { com.example.composespeedtest.data.Prefs(context.applicationContext) }
    
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showPrivacyDetails by remember { mutableStateOf(false) }
    var showAdsSettings by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.h6, color = Color.White)
        
        // Appearance Section
        SettingsSection(title = "Appearance") {
            Card(
                backgroundColor = Color(0xFF151B2C),
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme", color = Color.White, fontWeight = FontWeight.Medium)
                    Text("Choose your preferred theme", style = MaterialTheme.typography.caption, color = LightColor2)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(selected = !darkMode, onClick = { onDarkModeChange(false) })
                            Spacer(Modifier.width(6.dp))
                            Text("Light", color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(selected = darkMode, onClick = { onDarkModeChange(true) })
                            Spacer(Modifier.width(6.dp))
                            Text("Dark", color = Color.White)
                        }
                    }
                }
            }
        }

        // Privacy & Data Section
        SettingsSection(title = "Privacy & Data") {
            Card(
                backgroundColor = Color(0xFF151B2C),
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsItem(
                        title = "Privacy Policy",
                        subtitle = "Read our complete privacy policy",
                        onClick = { showPrivacyPolicy = true }
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    SettingsItem(
                        title = "Data Usage Details",
                        subtitle = "How we handle your test data",
                        onClick = { showPrivacyDetails = true }
                    )
                }
            }
        }

        // Ads & Monetization Section
        SettingsSection(title = "Ads & Monetization") {
            Card(
                backgroundColor = Color(0xFF151B2C),
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsItem(
                        title = "Ad Settings",
                        subtitle = "Customize your ad experience",
                        onClick = { showAdsSettings = true }
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    SettingsItem(
                        title = "Remove Ads",
                        subtitle = "Support the app and remove ads",
                        onClick = { /* TODO: Implement premium purchase */ }
                    )
                }
            }
        }

        // About Section
        SettingsSection(title = "About") {
            Card(
                backgroundColor = Color(0xFF151B2C),
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsItem(
                        title = "App Info",
                        subtitle = "Version 1.0.0 • Build 1",
                        onClick = { showAboutDialog = true }
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    SettingsItem(
                        title = "Rate App",
                        subtitle = "Help us improve with your feedback",
                        onClick = { /* TODO: Implement rating */ }
                    )
                }
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
    }

    // Privacy Details Dialog
    if (showPrivacyDetails) {
        PrivacyDetailsDialog(onDismiss = { showPrivacyDetails = false })
    }

    // Ads Settings Dialog
    if (showAdsSettings) {
        AdsSettingsDialog(
            prefs = prefs,
            onDismiss = { showAdsSettings = false }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}
@Composable
fun GaugeStat(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.overline, color = LightColor)
        Text(text = "%.1f".format(value), style = MaterialTheme.typography.subtitle2, color = Color.White)
    }
}

@Composable
fun EnhancedGaugeStat(label: String, value: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.overline, 
            color = color.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "%.1f".format(value), 
            style = MaterialTheme.typography.h6, 
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SpeedSparkline(samples: List<Float>) {
    val maxVal = (samples.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (samples.size < 2) return@Canvas
        val widthStep = size.width / (samples.size - 1)
        var prev = Offset(0f, size.height - (samples[0] / maxVal) * size.height)
        for (i in 1 until samples.size) {
            val x = i * widthStep
            val y = size.height - (samples[i] / maxVal) * size.height
            val cur = Offset(x, y)
            drawLine(
                color = Teal200,
                start = prev,
                end = cur,
                strokeWidth = 3f
            )
            prev = cur
        }
    }
}

@Composable
fun EnhancedSpeedSparkline(samples: List<Float>) {
    val maxVal = (samples.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (samples.size < 2) return@Canvas
        
        // Draw gradient background
        val gradient = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Teal200.copy(alpha = 0.1f),
                Teal200.copy(alpha = 0.05f),
                Color.Transparent
            )
        )
        
        // Create path for filled area
        val path = Path()
        val widthStep = size.width / (samples.size - 1)
        
        // Start from bottom left
        path.moveTo(0f, size.height)
        
        // Draw the line
        for (i in samples.indices) {
            val x = i * widthStep
            val y = size.height - (samples[i] / maxVal) * size.height
            if (i == 0) {
                path.lineTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Complete the path to bottom right
        path.lineTo(size.width, size.height)
        path.close()
        
        // Draw filled area
        drawPath(path, brush = gradient)
        
        // Draw the line
        var prev = Offset(0f, size.height - (samples[0] / maxVal) * size.height)
        for (i in 1 until samples.size) {
            val x = i * widthStep
            val y = size.height - (samples[i] / maxVal) * size.height
            val cur = Offset(x, y)
            drawLine(
                color = Teal200,
                start = prev,
                end = cur,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            prev = cur
        }
        
        // Draw data points
        for (i in samples.indices) {
            val x = i * widthStep
            val y = size.height - (samples[i] / maxVal) * size.height
            drawCircle(
                color = Teal200,
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF00E676),
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 8.dp),
        modifier = Modifier
            .height(52.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp))
    ) {
        Text(
            text = text, 
            modifier = Modifier.padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        backgroundColor = Color(0xFF151B2C),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.caption, color = LightColor)
            Text(text = value, style = MaterialTheme.typography.h6, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EnhancedStatCard(
    title: String, 
    value: String, 
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        backgroundColor = Color(0xFF1A1F3A),
        shape = RoundedCornerShape(20.dp),
        elevation = 6.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon, 
                fontSize = 24.sp
            )
            Text(
                text = title, 
                style = MaterialTheme.typography.caption, 
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value, 
                style = MaterialTheme.typography.h5, 
                color = color, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TestProgressCard(state: SpeedTestUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundColor = Color(0xFF1A1F3A),
        shape = RoundedCornerShape(20.dp),
        elevation = 8.dp,
        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🔄", fontSize = 20.sp)
                Text(
                    text = "Test in Progress",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LinearProgressIndicator(
                progress = state.progress,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00E676),
                backgroundColor = Color.White.copy(alpha = 0.1f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.currentTest,
                    style = MaterialTheme.typography.body2,
                    color = LightColor2,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(state.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundColor = Color(0xFF1A1F3A),
        shape = RoundedCornerShape(20.dp),
        elevation = 6.dp,
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("⚠️", fontSize = 20.sp)
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                color = Color.Red.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SpeedometerGauge(
    currentSpeedMbps: Float,
    maxSpeedMbps: Float,
    progress: Float,
    isRunning: Boolean
) {
    val targetRatio = (currentSpeedMbps / maxSpeedMbps).coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = if (isRunning) 450 else 800)
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        drawSpeedometer(animatedRatio, progress, isRunning, maxSpeedMbps)
    }
}

fun DrawScope.drawSpeedometer(
    ratio: Float,
    progress: Float,
    isRunning: Boolean,
    maxSpeed: Float
) {
    // Gauge geometry
    val startAngle = 150f
    val sweep = 240f
    val strokeWidth = 18f
    val inset = 40f
    val topLeft = Offset(inset, inset)
    val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)

    // Background arc
    drawArc(
        color = Color.White.copy(alpha = 0.08f),
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Segmented progress arcs (green->yellow->orange->red)
    val segments = listOf(
        0.0f to 0.25f,
        0.25f to 0.5f,
        0.5f to 0.75f,
        0.75f to 1.0f
    )
    val colors = listOf(
        Color(0xFF00E676), // green
        Color(0xFFFFEB3B), // yellow
        Color(0xFFFF9800), // orange
        Color(0xFFF44336)  // red
    )
    segments.forEachIndexed { index, (from, to) ->
        val segStart = startAngle + sweep * from
        val segEnd = startAngle + sweep * to
        val segSweep = (segEnd - segStart)
        val filled = ((ratio - from) / (to - from)).coerceIn(0f, 1f)
        if (filled > 0f) {
            drawArc(
                color = colors[index],
                startAngle = segStart,
                sweepAngle = segSweep * filled,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    // Tick marks (major + minor)
    val center = Offset(topLeft.x + arcSize.width / 2, topLeft.y + arcSize.height / 2)
    val radius = arcSize.width / 2
    val majorTickCount = 9 // every 30 degrees across 240° -> 9 ticks
    val minorPerMajor = 4
    repeat((majorTickCount - 1) * (minorPerMajor + 1) + 1) { idx ->
        val t = idx / ((majorTickCount - 1f) * (minorPerMajor + 1))
        val angle = (startAngle + sweep * t) * (Math.PI / 180f)
        val isMajor = idx % (minorPerMajor + 1) == 0
        val inner = radius - if (isMajor) 22f else 12f
        val outer = radius - 2f
        val sx = center.x + inner * kotlin.math.cos(angle).toFloat()
        val sy = center.y + inner * kotlin.math.sin(angle).toFloat()
        val ex = center.x + outer * kotlin.math.cos(angle).toFloat()
        val ey = center.y + outer * kotlin.math.sin(angle).toFloat()
        drawLine(
            color = if (isMajor) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.35f),
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = if (isMajor) 3f else 2f
        )
    }

    // Numeric labels at majors
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 28f
            alpha = (0.85f * 255).toInt()
        }
        repeat(majorTickCount) { i ->
            val tt = i / (majorTickCount - 1f)
            val ang = (startAngle + sweep * tt) * (Math.PI / 180f)
            val rLabel = radius - 40f
            val lx = center.x + rLabel * kotlin.math.cos(ang).toFloat()
            val ly = center.y + rLabel * kotlin.math.sin(ang).toFloat()
            val value = (maxSpeed * tt).toInt().toString()
            val textWidth = paint.measureText(value)
            canvas.nativeCanvas.drawText(value, lx - textWidth / 2f, ly + 10f, paint)
        }
    }

    // Needle (triangular) with glow and hub ring
    val needleAngle = (startAngle + sweep * ratio) * (Math.PI / 180f)
    val needleLength = radius - 28f
    val baseWidth = 16f
    val tip = Offset(
        x = center.x + needleLength * kotlin.math.cos(needleAngle).toFloat(),
        y = center.y + needleLength * kotlin.math.sin(needleAngle).toFloat()
    )
    val baseLeftAngle = needleAngle + Math.PI / 2
    val baseRightAngle = needleAngle - Math.PI / 2
    val baseCenter = center
    val left = Offset(
        x = baseCenter.x + (baseWidth / 2) * kotlin.math.cos(baseLeftAngle).toFloat(),
        y = baseCenter.y + (baseWidth / 2) * kotlin.math.sin(baseLeftAngle).toFloat()
    )
    val right = Offset(
        x = baseCenter.x + (baseWidth / 2) * kotlin.math.cos(baseRightAngle).toFloat(),
        y = baseCenter.y + (baseWidth / 2) * kotlin.math.sin(baseRightAngle).toFloat()
    )
    val needlePath = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }
    // Glow
    drawPath(
        path = needlePath,
        color = Teal200.copy(alpha = 0.35f)
    )
    // Needle body
    drawPath(
        path = needlePath,
        color = Color.White.copy(alpha = 0.95f)
    )
    // Hub ring
    drawCircle(color = Color.White, radius = 10f, center = center)
    drawCircle(color = DarkColor, radius = 6f, center = center)

    // Subtle orbiting dots when running
    if (isRunning) {
        val dotCount = 12
        val orbitR = radius - 8f
        val phase = (progress * 360f) * (Math.PI / 180f)
        repeat(dotCount) { i ->
            val a = (startAngle + i * (sweep / dotCount)) * (Math.PI / 180f) + phase
            val dx = center.x + orbitR * kotlin.math.cos(a).toFloat()
            val dy = center.y + orbitR * kotlin.math.sin(a).toFloat()
            drawCircle(color = Teal200.copy(alpha = 0.5f), radius = 3f, center = Offset(dx, dy))
        }
    }

    // Highlight sweep overlay
    drawArc(
        color = Color.White.copy(alpha = 0.06f),
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
}

// Settings UI Components
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.caption,
                color = LightColor2
            )
        }
        Icon(
            Icons.Default.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = LightColor2,
            modifier = Modifier.size(16.dp)
        )
    }
}

// Privacy Policy Dialog
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Privacy Policy",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Last updated: ${java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
                    style = MaterialTheme.typography.caption,
                    color = LightColor2
                )
                
                Text(
                    "Information We Collect",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "• Speed test results (download/upload speeds, ping, jitter)\n" +
                    "• Network type and connection details\n" +
                    "• App usage statistics (anonymized)\n" +
                    "• Device information (model, OS version)",
                    color = Color.White
                )
                
                Text(
                    "How We Use Your Information",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "• Provide accurate speed test results\n" +
                    "• Improve app performance and features\n" +
                    "• Analyze network performance trends\n" +
                    "• Display relevant advertisements",
                    color = Color.White
                )
                
                Text(
                    "Data Sharing",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "We do not sell your personal data. We may share anonymized, aggregated data with:\n" +
                    "• Ad networks for relevant advertising\n" +
                    "• Analytics providers for app improvement\n" +
                    "• Speed test servers for accurate measurements",
                    color = Color.White
                )
                
                Text(
                    "Your Rights",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "You have the right to:\n" +
                    "• Access your data\n" +
                    "• Request data deletion\n" +
                    "• Opt out of personalized ads\n" +
                    "• Contact us with privacy concerns",
                    color = Color.White
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Teal200)
            }
        },
        backgroundColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    )
}

// Privacy Details Dialog
@Composable
fun PrivacyDetailsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Data Usage Details",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Speed Test Data",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "During speed tests, we transfer random data to measure your connection. This includes:\n" +
                    "• Download tests: Fetch test files from public servers\n" +
                    "• Upload tests: Send data to speed measurement endpoints\n" +
                    "• Ping tests: Send small packets to measure latency",
                    color = Color.White
                )
                
                Text(
                    "Test Servers",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "We use servers from:\n" +
                    "• Cloudflare (global CDN)\n" +
                    "• Hetzner (European servers)\n" +
                    "• ThinkBroadband (UK servers)\n" +
                    "• Other regional providers",
                    color = Color.White
                )
                
                Text(
                    "Data Retention",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "• Test results: Stored locally on your device\n" +
                    "• Usage analytics: Anonymized and retained for 12 months\n" +
                    "• No personal information is collected or stored",
                    color = Color.White
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Teal200)
            }
        },
        backgroundColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    )
}

// Ads Settings Dialog
@Composable
fun AdsSettingsDialog(
    prefs: com.example.composespeedtest.data.Prefs,
    onDismiss: () -> Unit
) {
    var personalizedAds by remember { mutableStateOf(true) }
    var showAdFrequency by remember { mutableStateOf("Normal") }
    
    // Load current preferences
    LaunchedEffect(Unit) {
        prefs.personalizedAds.collect { personalizedAds = it }
        prefs.adFrequency.collect { showAdFrequency = it }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ad Settings",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Personalized Ads Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Personalized Ads",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Show ads based on your interests",
                            style = MaterialTheme.typography.caption,
                            color = LightColor2
                        )
                    }
                    Switch(
                        checked = personalizedAds,
                        onCheckedChange = { 
                            personalizedAds = it
                            kotlinx.coroutines.GlobalScope.launch { 
                                prefs.setPersonalizedAds(it) 
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Teal200,
                            checkedTrackColor = Teal200.copy(alpha = 0.5f)
                        )
                    )
                }
                
                Divider(color = Color.White.copy(alpha = 0.1f))
                
                // Ad Frequency
                Text(
                    "Ad Frequency",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Normal", "High").forEach { frequency ->
                        Button(
                            onClick = { 
                                showAdFrequency = frequency
                                kotlinx.coroutines.GlobalScope.launch { 
                                    prefs.setAdFrequency(frequency) 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (showAdFrequency == frequency) Teal200 else Color.White.copy(alpha = 0.1f),
                                contentColor = if (showAdFrequency == frequency) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = frequency,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Divider(color = Color.White.copy(alpha = 0.1f))
                
                // Ad Types
                Text(
                    "Ad Types",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "• Banner ads: Displayed at the bottom\n" +
                    "• Interstitial ads: Full-screen between tests\n" +
                    "• Rewarded ads: Optional for premium features",
                    style = MaterialTheme.typography.caption,
                    color = LightColor2
                )
                
                Divider(color = Color.White.copy(alpha = 0.1f))
                
                // Premium Option
                Card(
                    backgroundColor = Teal200.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Remove All Ads",
                            color = Teal200,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Upgrade to premium to enjoy an ad-free experience",
                            style = MaterialTheme.typography.caption,
                            color = LightColor2
                        )
                        Button(
                            onClick = { /* TODO: Implement premium purchase */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Teal200, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upgrade Now")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Save", color = Teal200)
            }
        },
        backgroundColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    )
}

// About Dialog
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "About Speed Test",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Version 1.0.0",
                    color = Teal200,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "A modern, accurate internet speed testing app built with Jetpack Compose.",
                    color = Color.White
                )
                
                Text(
                    "Features:",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "• Real-time speed measurements\n" +
                    "• Beautiful animated gauge\n" +
                    "• Detailed network statistics\n" +
                    "• Dark/Light theme support\n" +
                    "• Privacy-focused design",
                    color = Color.White
                )
                
                Text(
                    "Built with ❤️ using:",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "• Jetpack Compose\n" +
                    "• Kotlin Coroutines\n" +
                    "• Material Design 3\n" +
                    "• DataStore for preferences",
                    color = Color.White
                )
                
                Text(
                    "© 2024 Speed Test App. All rights reserved.",
                    style = MaterialTheme.typography.caption,
                    color = LightColor2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Teal200)
            }
        },
        backgroundColor = Color(0xFF1E1E1E),
        contentColor = Color.White
    )
}

