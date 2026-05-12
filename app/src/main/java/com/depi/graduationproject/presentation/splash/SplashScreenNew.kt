package com.depi.graduationproject.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.DeepPurple
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.ScannerLogoMark
import com.depi.graduationproject.presentation.components.ScannerLogoMarkWithGlow

@Composable
fun SplashScreenContent(
    isLoading: Boolean,
    isReady: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                ScannerLogoMarkWithGlow(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale)
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonPink, DeepPurple)),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ScannerLogoMark(size = 60.dp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Text(
                        text = "LPR-Edge",
                        style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.Text(
                        text = "Automating Garage Management\nwith Edge-AI",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            if (error != null) {
                androidx.compose.material3.Text(
                    text = error,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                com.depi.graduationproject.presentation.components.SecondaryButton(
                    text = "RETRY",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GradientButton(
                    text = if (isLoading) "INITIALIZING..." else "GET STARTED",
                    onClick = onGetStarted,
                    isLoading = isLoading,
                    enabled = isReady,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
