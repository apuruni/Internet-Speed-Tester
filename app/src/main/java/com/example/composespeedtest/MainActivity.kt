package com.example.composespeedtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composespeedtest.data.Prefs
import com.example.composespeedtest.navigation.NavRoutes
import com.example.composespeedtest.ui.OnboardingScreen
import com.example.composespeedtest.ui.SplashScreen
import com.example.composespeedtest.ui.theme.ComposeSpeedTestTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(applicationContext)

        setContent {
            var isDark by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) {
                prefs.darkTheme.collect { v -> isDark = v }
            }
            ComposeSpeedTestTheme(darkTheme = (isDark ?: true)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    var onboardingDone by remember { mutableStateOf<Boolean?>(null) }

                    LaunchedEffect(Unit) {
                        prefs.onboardingDone.collect { done ->
                            onboardingDone = done
                        }
                    }

                    if (onboardingDone == null || isDark == null) {
                        // Simple splash while loading prefs
                        SplashScreen { }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = NavRoutes.Splash
                        ) {
                            composable(NavRoutes.Splash) {
                                SplashScreen {
                                    if (onboardingDone == true) {
                                        navController.navigate(NavRoutes.Home) {
                                            popUpTo(NavRoutes.Splash) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(NavRoutes.Onboarding) {
                                            popUpTo(NavRoutes.Splash) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            composable(NavRoutes.Onboarding) {
                                OnboardingScreen {
                                    lifecycleScope.launch { prefs.setOnboardingDone(true) }
                                    navController.navigate(NavRoutes.Home) {
                                        popUpTo(NavRoutes.Onboarding) { inclusive = true }
                                    }
                                }
                            }
                            composable(NavRoutes.Home) {
                                SpeedTestScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

