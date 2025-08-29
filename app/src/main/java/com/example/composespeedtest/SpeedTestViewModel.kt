package com.example.composespeedtest

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {
    
    private val speedTestService = SpeedTestService(application)
    
    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()
    
    fun startSpeedTest() {
        if (_uiState.value.isTestRunning) {
            Log.d("SpeedTestViewModel", "Speed test already running, ignoring request")
            return
        }
        
        Log.d("SpeedTestViewModel", "Starting speed test...")
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isTestRunning = true,
                    progress = 0f,
                    currentTest = "Preparing measurement…",
                    errorMessage = null
                )
                
                Log.d("SpeedTestViewModel", "Speed test initialized, calling service...")
                
                speedTestService.performSpeedTest(
                    onProgress = { progress ->
                        Log.d("SpeedTestViewModel", "Progress update: ${progress}")
                        _uiState.value = _uiState.value.copy(progress = progress)
                    },
                    onResult = { result ->
                        Log.d("SpeedTestViewModel", "Speed test result received: $result")
                        
                        when (result) {
                            is SpeedTestService.SpeedTestResult.Loading -> {
                                _uiState.value = _uiState.value.copy(
                                    currentTest = "Measuring latency…"
                                )
                            }
                            is SpeedTestService.SpeedTestResult.Success -> {
                                Log.d("SpeedTestViewModel", "Speed test success: Download=${result.downloadSpeed}, Upload=${result.uploadSpeed}, Ping=${result.ping}")
                                
                                _uiState.value = _uiState.value.copy(
                                    isTestRunning = false,
                                    progress = 1f,
                                    currentTest = "Measurement complete",
                                    downloadSpeed = result.downloadSpeed,
                                    uploadSpeed = result.uploadSpeed,
                                    ping = result.ping,
                                    jitter = result.jitter,
                                    packetLoss = result.packetLoss,
                                    networkType = result.networkType,
                                    hasResults = true,
                                    errorMessage = null,
                                    minSpeed = _uiState.value.instantSamples.minOrNull() ?: 0f,
                                    avgSpeed = if (_uiState.value.instantSamples.isNotEmpty()) _uiState.value.instantSamples.average().toFloat() else 0f,
                                    maxSpeed = _uiState.value.instantSamples.maxOrNull() ?: 0f
                                )
                                
                                Log.d("SpeedTestViewModel", "UI state updated with results")
                            }
                            is SpeedTestService.SpeedTestResult.Error -> {
                                Log.e("SpeedTestViewModel", "Speed test error: ${result.message}")
                                
                                _uiState.value = _uiState.value.copy(
                                    isTestRunning = false,
                                    progress = 0f,
                                    currentTest = "Measurement failed",
                                    errorMessage = result.message
                                )
                            }
                        }
                    },
                    onInstantSpeed = { mbps ->
                        val list = (_uiState.value.instantSamples + mbps).takeLast(120)
                        _uiState.value = _uiState.value.copy(
                            instantSamples = list,
                            minSpeed = list.minOrNull() ?: 0f,
                            avgSpeed = if (list.isNotEmpty()) list.average().toFloat() else 0f,
                            maxSpeed = list.maxOrNull() ?: 0f
                        )
                    }
                )
                
                Log.d("SpeedTestViewModel", "Speed test service call completed")
                
            } catch (e: Exception) {
                Log.e("SpeedTestViewModel", "Exception during speed test", e)
                
                _uiState.value = _uiState.value.copy(
                    isTestRunning = false,
                    progress = 0f,
                    currentTest = "Test failed with exception",
                    errorMessage = "Exception: ${e.message}"
                )
            }
        }
    }
    
    fun resetTest() {
        Log.d("SpeedTestViewModel", "Resetting speed test")
        _uiState.value = SpeedTestUiState()
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d("SpeedTestViewModel", "ViewModel cleared, cleaning up service")
        speedTestService
    }
}

data class SpeedTestUiState(
    val isTestRunning: Boolean = false,
    val progress: Float = 0f,
    val currentTest: String = "",
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val ping: Int = 0,
    val jitter: Float = 0f,
    val packetLoss: Float = 0f,
    val networkType: String = "",
    val hasResults: Boolean = false,
    val errorMessage: String? = null,
    val instantSamples: List<Float> = emptyList(),
    val minSpeed: Float = 0f,
    val avgSpeed: Float = 0f,
    val maxSpeed: Float = 0f
)
