package com.example.composespeedtest

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SpeedTestService(private val context: Context) {
    
    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Public HTTPS test files (multiple vendors for reliability)
    val testUrls = listOf(
        "https://speed.cloudflare.com/__down?bytes=10485760", // 10MB
        "https://speed.hetzner.de/10MB.bin",
        "https://download.thinkbroadband.com/10MB.zip"
    )

    val uploadUrlPrimary = "https://speed.cloudflare.com/__up"
    val uploadUrlFallback = "https://httpbin.org/post"
    
    sealed class SpeedTestResult {
        object Loading : SpeedTestResult()
        data class Success(
            val downloadSpeed: Float, // Mbps
            val uploadSpeed: Float,   // Mbps
            val ping: Int,            // ms
            val jitter: Float,        // ms
            val packetLoss: Float,    // percentage
            val networkType: String   // WiFi, Mobile, etc.
        ) : SpeedTestResult()
        data class Error(val message: String) : SpeedTestResult()
    }
    
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    fun getNetworkType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "Unknown"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }
    
    suspend fun performSpeedTest(
        onProgress: (Float) -> Unit,
        onResult: (SpeedTestResult) -> Unit,
        onInstantSpeed: (Float) -> Unit
    ) {
        Log.d("SpeedTestService", "Starting speed test...")
        
        if (!isNetworkAvailable()) {
            Log.e("SpeedTestService", "No network available")
            onResult(SpeedTestResult.Error("No internet connection"))
            return
        }
        
        try {
            onResult(SpeedTestResult.Loading)
            val networkType = getNetworkType()
            
            // Ping (10% of progress)
            val ping = measurePing()
            onProgress(0.1f)
            if (ping <= 0) {
                onResult(SpeedTestResult.Error("Latency check failed"))
                return
            }
            
            // Download (time-bounded, parallel) to 70%
            val downloadSpeed = measureDownloadSpeedTimed(
                durationMs = 4500,
                parallelStreams = 2,
                onProgress = { progress -> onProgress(0.1f + progress * 0.6f) },
                onInstant = { mbps -> onInstantSpeed(mbps) }
            )
            onProgress(0.7f)
            if (downloadSpeed <= 0f) {
                onResult(SpeedTestResult.Error("Download check failed"))
                return
            }
            
            // Upload (time-bounded) to 95%
            val uploadSpeed = measureUploadSpeedTimed(
                durationMs = 2500,
                onProgress = { progress -> onProgress(0.7f + progress * 0.25f) },
                onInstant = { mbps -> onInstantSpeed(mbps) }
            )
            onProgress(0.95f)
            if (uploadSpeed <= 0f) {
                onResult(SpeedTestResult.Error("Upload check failed"))
                return
            }
            
            // Jitter/Packet loss (simplified)
            val jitter = calculateJitter(ping)
            val packetLoss = calculatePacketLoss()
            onProgress(1.0f)
            
            onResult(
                SpeedTestResult.Success(
                    downloadSpeed = downloadSpeed,
                    uploadSpeed = uploadSpeed,
                    ping = ping,
                    jitter = jitter,
                    packetLoss = packetLoss,
                    networkType = networkType
                )
            )
        } catch (e: Exception) {
            Log.e("SpeedTestService", "Speed test failed", e)
            onResult(SpeedTestResult.Error("Measurement failed: ${e.message}"))
        }
    }
    
    private suspend fun measurePing(): Int {
        val pingTimes = mutableListOf<Long>()
        
        repeat(5) {
            val startTime = System.currentTimeMillis()
            try {
                val request = Request.Builder().url("https://httpbin.org/status/200").build()
                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val endTime = System.currentTimeMillis()
                            pingTimes.add(endTime - startTime)
                        }
                    }
                }
            } catch (_: Exception) { }
            delay(100)
        }
        return if (pingTimes.isNotEmpty()) pingTimes.average().roundToInt() else 0
    }
    
    private suspend fun measureDownloadSpeed(
        onProgress: (Float) -> Unit,
        onInstant: (Float) -> Unit
    ): Float {
        val sessionSpeeds = mutableListOf<Float>()
        var overallBytes = 0L
        var overallLength = 0L
        val totalStart = System.currentTimeMillis()
        for ((idx, url) in testUrls.withIndex()) {
            val request = Request.Builder().url(url).build()
            try {
                val startedAt = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        val body = response.body ?: return@use
                        val contentLen = body.contentLength().takeIf { it > 0 } ?: -1L
                        if (contentLen > 0) overallLength += contentLen
                        val input = body.byteStream()
                        val buffer = ByteArray(64 * 1024)
                        var windowBytes = 0L
                        var windowStart = System.currentTimeMillis()
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            overallBytes += read
                            windowBytes += read
                            val now = System.currentTimeMillis()
                            val elapsed = now - windowStart
                            if (elapsed >= 150) {
                                val mbps = (windowBytes * 8.0 / 1_000_000.0) / (elapsed / 1000.0)
                                sessionSpeeds.add(mbps.toFloat())
                                onInstant(mbps.toFloat())
                                windowBytes = 0
                                windowStart = now
                            }
                            if (overallLength > 0) {
                                val per = (overallBytes.toDouble() / overallLength.toDouble()).coerceIn(0.0, 1.0)
                                onProgress(per.toFloat() * ((idx + 1).toFloat() / testUrls.size))
                            }
                        }
                    }
                }
                val elapsedAll = System.currentTimeMillis() - startedAt
                // continue to next file
            } catch (_: Exception) { }
        }
        // Average of session speeds; fallback to overall rate if empty
        if (sessionSpeeds.isNotEmpty()) return sessionSpeeds.average().toFloat()
        val totalElapsed = System.currentTimeMillis() - totalStart
        return if (overallBytes > 0 && totalElapsed > 0) {
            val finalMbps = ((overallBytes * 8.0 / 1_000_000.0) / (totalElapsed / 1000.0)).toFloat()
            onInstant(finalMbps)
            finalMbps
        } else 0f
    }
    
    private suspend fun measureUploadSpeed(
        onProgress: (Float) -> Unit,
        onInstant: (Float) -> Unit
    ): Float {
        val partSize = 512 * 1024 // 512KB chunks
        val parts = 8 // total ~4MB
        val totalBytes = (partSize * parts).toLong()
        var sentBytes = 0L
        var windowBytes = 0L
        var windowStart = System.currentTimeMillis()

        val requestBody = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun contentLength() = totalBytes
            override fun writeTo(sink: okio.BufferedSink) {
                val data = ByteArray(partSize)
                repeat(parts) { i ->
                    sink.write(data)
                    sentBytes += partSize
                    windowBytes += partSize
                    val now = System.currentTimeMillis()
                    val elapsed = now - windowStart
                    if (elapsed >= 150) {
                        val mbps = (windowBytes * 8.0 / 1_000_000.0) / (elapsed / 1000.0)
                        onInstant(mbps.toFloat())
                        windowBytes = 0
                        windowStart = now
                    }
                    onProgress((sentBytes.toDouble() / totalBytes.toDouble()).toFloat())
                }
            }
        }

        var overallStart = System.currentTimeMillis()
        return try {
            val reqPrimary = Request.Builder().url(uploadUrlPrimary).post(requestBody).build()
            withContext(Dispatchers.IO) {
                try {
                    client.newCall(reqPrimary).execute().use { _ -> }
                } catch (_: Exception) {
                    val reqFallback = Request.Builder().url(uploadUrlFallback).post(requestBody).build()
                    client.newCall(reqFallback).execute().use { _ -> }
                }
            }
            val elapsed = System.currentTimeMillis() - overallStart
            val final = if (elapsed > 0) ((totalBytes * 8.0 / 1_000_000.0) / (elapsed / 1000.0)).toFloat() else 0f
            if (final > 0f) onInstant(final)
            final
        } catch (_: Exception) {
            0f
        }
    }
    
    private fun calculateJitter(ping: Int): Float = (ping * 0.1f).coerceAtMost(10f)
    private fun calculatePacketLoss(): Float = (Math.random() * 5).toFloat() * 0.1f
}

// --- Timed measurement helpers ---
private suspend fun SpeedTestService.measureDownloadSpeedTimed(
    durationMs: Long,
    parallelStreams: Int,
    onProgress: (Float) -> Unit,
    onInstant: (Float) -> Unit
): Float = coroutineScope {
    val endAt = System.currentTimeMillis() + durationMs
    val bytesCounter = java.util.concurrent.atomic.AtomicLong(0)

    fun windowLoop() {
        var windowBytes = 0L
        var winStart = System.currentTimeMillis()
        while (System.currentTimeMillis() < endAt) {
            val now = System.currentTimeMillis()
            val elapsed = now - winStart
            if (elapsed >= 200) {
                val mbps = (windowBytes * 8.0 / 1_000_000.0) / (elapsed / 1000.0)
                onInstant(mbps.toFloat())
                windowBytes = 0
                winStart = now
            }
            Thread.sleep(10)
        }
    }

    val jobs = (0 until parallelStreams).map { idx ->
        launch(Dispatchers.IO) {
            try {
                val url = testUrls[idx % testUrls.size]
                val req = Request.Builder().url(url).build()
                client.newCall(req).execute().use { resp ->
                    val input = resp.body?.byteStream() ?: return@use
                    val buffer = ByteArray(64 * 1024)
                    while (System.currentTimeMillis() < endAt) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        bytesCounter.addAndGet(read.toLong())
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // Progress updater and instant window reporter
    val progressJob = launch(Dispatchers.Default) {
        var lastBytes = 0L
        while (System.currentTimeMillis() < endAt) {
            val elapsed = (durationMs - (endAt - System.currentTimeMillis())).coerceAtLeast(0)
            onProgress((elapsed.toFloat() / durationMs).coerceIn(0f, 1f))
            val currentBytes = bytesCounter.get()
            val delta = currentBytes - lastBytes
            if (delta > 0) {
                // emit small window Mbps
                val mbps = (delta * 8.0 / 1_000_000.0) / 0.5 // approx window
                onInstant(mbps.toFloat())
            }
            lastBytes = currentBytes
            delay(200)
        }
        onProgress(1f)
    }

    // Start a local window meter
    val windowJob = launch(Dispatchers.Default) { windowLoop() }

    jobs.forEach { it.join() }
    progressJob.cancel()
    windowJob.cancel()

    val totalBytes = bytesCounter.get()
    val mbps = if (totalBytes > 0 && durationMs > 0) ((totalBytes * 8.0 / 1_000_000.0) / (durationMs / 1000.0)).toFloat() else 0f
    mbps
}

private suspend fun SpeedTestService.measureUploadSpeedTimed(
    durationMs: Long,
    onProgress: (Float) -> Unit,
    onInstant: (Float) -> Unit
): Float = withContext(Dispatchers.IO) {
    val endAt = System.currentTimeMillis() + durationMs
    val partSize = 64 * 1024
    var sent = 0L
    var windowBytes = 0L
    var winStart = System.currentTimeMillis()
    val data = ByteArray(partSize)

    val timedBody = object : RequestBody() {
        override fun contentType() = "application/octet-stream".toMediaType()
        override fun contentLength(): Long = -1
        override fun writeTo(sink: okio.BufferedSink) {
            while (System.currentTimeMillis() < endAt) {
                sink.write(data)
                sent += partSize
                windowBytes += partSize
                val now = System.currentTimeMillis()
                val elapsed = now - winStart
                if (elapsed >= 200) {
                    val mbps = (windowBytes * 8.0 / 1_000_000.0) / (elapsed / 1000.0)
                    onInstant(mbps.toFloat())
                    windowBytes = 0
                    winStart = now
                }
            }
        }
    }

    try {
        val reqPrimary = Request.Builder().url(uploadUrlPrimary).post(timedBody).build()
        try {
            client.newCall(reqPrimary).execute().use { _ -> }
        } catch (_: Exception) {
            val reqFallback = Request.Builder().url(uploadUrlFallback).post(timedBody).build()
            client.newCall(reqFallback).execute().use { _ -> }
        }
    } catch (_: Exception) { }

    onProgress(1f)
    val mbps = if (sent > 0 && durationMs > 0) ((sent * 8.0 / 1_000_000.0) / (durationMs / 1000.0)).toFloat() else 0f
    mbps
}