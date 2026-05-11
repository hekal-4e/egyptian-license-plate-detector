package com.depi.graduationproject.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.Background
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.SecondaryButton
import androidx.compose.ui.tooling.preview.Preview
import com.depi.graduationproject.core.theme.GraduationProjectTheme

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SplashContent(
        uiState = uiState,
        onRetry = { viewModel.initialize() },
        onNavigateNext = onNavigateNext
    )
}

@Composable
private fun SplashContent(
    uiState: SplashUiState,
    onRetry: () -> Unit,
    onNavigateNext: () -> Unit
) {
    // Animation for the glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clip(CircleShape)
                        .background(NeonPink.copy(alpha = 0.2f))
                )
            }
            
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scanner",
                    tint = NeonPink,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "LPR-Edge",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "AI-Powered License Plate Recognition",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SecondaryButton(
                text = "RETRY",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            GradientButton(
                text = if (uiState.isLoading) "INITIALIZING AI..." else "GET STARTED →",
                onClick = onNavigateNext,
                isLoading = uiState.isLoading,
                enabled = uiState.isReady,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun SplashPreview() {
    GraduationProjectTheme {
        SplashContent(
            uiState = SplashUiState(isLoading = false, isReady = true),
            onRetry = {},
            onNavigateNext = {}
        )
    }
}