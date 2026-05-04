package com.depi.graduationproject.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText

@Composable
fun MetricCard(
    label: String,
    value: String,
    subtitle: String,
    subtitleColor: Color = SecondaryText,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large) // 16dp
            .background(CardSurface)
            .padding(16.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = SecondaryText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                } else {
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> height } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            label = "metric_animation"
        ) { targetValue ->
            Text(
                text = targetValue,
                color = PrimaryText,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = subtitle,
            color = subtitleColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun MetricCardPreview() {
    GraduationProjectTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MetricCard(
                label = "ACTIVE SESSIONS",
                value = "42",
                subtitle = "Spots Filled",
                subtitleColor = EmeraldGreen
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MetricCard(
                label = "TODAY'S REVENUE",
                value = "EGP 840",
                subtitle = "Last updated 2m ago"
            )
        }
    }
}
