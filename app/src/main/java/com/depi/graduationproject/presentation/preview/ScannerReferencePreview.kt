package com.depi.graduationproject.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.InstructionChip
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PanelSurfaceAlt
import com.depi.graduationproject.core.theme.PinkHot
import com.depi.graduationproject.core.theme.PrimaryHorizontalGradient
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.presentation.components.PlateTargetFrame
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun ScannerReferencePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LprDimens.ScreenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(LprDimens.IconCircleSize)
                        .clip(CircleShape)
                        .background(PanelSurfaceAlt.copy(alpha = 0.8f))
                        .semantics { contentDescription = "Close scanner" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(LprDimens.IconCircleSize)
                            .clip(CircleShape)
                            .background(PanelSurfaceAlt.copy(alpha = 0.8f))
                            .semantics { contentDescription = "Toggle flashlight" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Flashlight",
                            tint = PrimaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PanelSurface.copy(alpha = 0.8f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .semantics { contentDescription = "AI Active" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PinkHot,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "AI Active",
                                style = InstructionChip,
                                color = PrimaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PlateTargetFrame()
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelSurfaceAlt.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Position license plate within frame",
                        style = InstructionChip,
                        color = PrimaryText.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "Hold steady for automatic capture",
                    style = InstructionChip,
                    color = PrimaryText.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .size(LprDimens.CaptureButtonSize)
                        .clip(CircleShape)
                        .background(AppBackground)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(PrimaryHorizontalGradient)
                        .clickable {}
                        .semantics { contentDescription = "Capture plate" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }

                Text(
                    text = "Can't scan? Enter manually",
                    style = InstructionChip.copy(textDecoration = TextDecoration.Underline),
                    color = PrimaryText.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {}
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
