package com.example.composespeedtest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
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
            backgroundColor = Color(0xFF151B2C),
            shape = RoundedCornerShape(20.dp),
            elevation = 0.dp
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

        // Current speed below gauge (centered)
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
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
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                maxLines = 1
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.caption,
                color = LightColor
            )
        }

        // Min / Avg / Max below gauge for cleaner alignment
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GaugeStat(label = "MIN", value = uiState.minSpeed)
            GaugeStat(label = "AVG", value = uiState.avgSpeed)
            GaugeStat(label = "MAX", value = uiState.maxSpeed)
        }

        // Sparkline
        if (uiState.instantSamples.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                backgroundColor = Color(0xFF151B2C),
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp
            ) {
                Box(modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(12.dp)) {
                    SpeedSparkline(samples = uiState.instantSamples)
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
            StatCard(title = "Download", value = if (uiState.hasResults) "%.1f Mbps".format(uiState.downloadSpeed) else "--", modifier = Modifier.weight(1f))
            StatCard(title = "Upload", value = if (uiState.hasResults) "%.1f Mbps".format(uiState.uploadSpeed) else "--", modifier = Modifier.weight(1f))
        }

        // Stats Row 2
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(title = "Ping", value = if (uiState.hasResults) "${uiState.ping} ms" else "--", modifier = Modifier.weight(1f))
            StatCard(title = "Jitter", value = if (uiState.hasResults) "%.1f ms".format(uiState.jitter) else "--", modifier = Modifier.weight(1f))
        }

        // Footer row
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Packet Loss: ${if (uiState.hasResults) "%.1f%%".format(uiState.packetLoss) else "--"}",
                style = MaterialTheme.typography.caption,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = if (uiState.hasResults && uiState.networkType.isNotEmpty()) uiState.networkType else "",
                style = MaterialTheme.typography.caption,
                color = Color.White.copy(alpha = 0.8f)
            )
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Status", style = MaterialTheme.typography.h6, color = Color.White)
        if (!uiState.hasResults) {
            Text(
                text = "No recent measurement. Start a test to see results here.",
                style = MaterialTheme.typography.caption,
                color = LightColor2
            )
        }
        StatCard(title = "Download", value = if (uiState.hasResults) "%.1f Mbps".format(uiState.downloadSpeed) else "--")
        StatCard(title = "Upload", value = if (uiState.hasResults) "%.1f Mbps".format(uiState.uploadSpeed) else "--")
        StatCard(title = "Latency", value = if (uiState.hasResults) "${uiState.ping} ms" else "--")
        StatCard(title = "Jitter", value = if (uiState.hasResults) "%.1f ms".format(uiState.jitter) else "--")
        StatCard(title = "Packet loss", value = if (uiState.hasResults) "%.1f%%".format(uiState.packetLoss) else "--")
        if (uiState.instantSamples.isNotEmpty()) {
            Card(
                backgroundColor = Color(0xFF151B2C),
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp
            ) {
                Box(modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .padding(12.dp)) {
                    SpeedSparkline(samples = uiState.instantSamples)
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGradient)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.h6, color = Color.White)
        Card(
            backgroundColor = Color(0xFF151B2C),
            shape = RoundedCornerShape(16.dp),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Appearance", color = Color.White)
                Text("Choose your theme", style = MaterialTheme.typography.caption, color = LightColor2)
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

        // Privacy section
        var showPrivacy by remember { mutableStateOf(false) }
        Text("Privacy", style = MaterialTheme.typography.subtitle1, color = Color.White)
        Card(
            backgroundColor = Color(0xFF151B2C),
            shape = RoundedCornerShape(16.dp),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Privacy & data usage", color = Color.White)
                    Text("Tap to view details", style = MaterialTheme.typography.caption, color = LightColor2)
                }
                Button(
                    onClick = { showPrivacy = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Teal200, contentColor = Color.Black)
                ) { Text("View") }
            }
        }
        if (showPrivacy) {
            AlertDialog(
                onDismissRequest = { showPrivacy = false },
                title = { Text("Privacy & Data Usage") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("We transfer test data (random bytes) to measure your connection speed. We do not collect personal information.")
                        Text("Downloads use public test files (Cloudflare, Hetzner, ThinkBroadband). Uploads post to speed endpoints.")
                        Text("Your IP may be visible to those servers as with any internet request.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacy = false }) { Text("Close") }
                }
            )
        }
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
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Green500,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        modifier = Modifier
            .height(44.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 16.dp))
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
fun TestProgressCard(state: SpeedTestUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundColor = Color(0xFF151B2C),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Test in Progress",
                style = MaterialTheme.typography.subtitle1,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = state.progress,
                modifier = Modifier.fillMaxWidth(),
                color = Green500,
                backgroundColor = Color.White.copy(alpha = 0.1f)
            )
            Text(
                text = state.currentTest,
                style = MaterialTheme.typography.body2,
                color = LightColor2
            )
            Text(
                text = "${(state.progress * 100).toInt()}%",
                style = MaterialTheme.typography.caption,
                color = LightColor
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundColor = Color.Red.copy(alpha = 0.18f),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.body2,
            color = Color.Red.copy(alpha = 0.9f)
        )
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

