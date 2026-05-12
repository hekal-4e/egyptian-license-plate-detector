package com.depi.graduationproject.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.Background
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.SecondaryButton

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SplashScreenContent(
        isLoading = uiState.isLoading,
        isReady = uiState.isReady,
        error = uiState.error,
        onRetry = { viewModel.initialize() },
        onGetStarted = onNavigateNext,
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    )
}
