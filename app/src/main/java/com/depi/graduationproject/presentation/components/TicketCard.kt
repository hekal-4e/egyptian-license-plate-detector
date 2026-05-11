package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.utils.QRCodeGenerator

@Composable
fun TicketCard(
    plateNumbers: String,
    plateLetters: String,
    zone: String,
    entryTime: String,
    date: String,
    modifier: Modifier = Modifier,
    qrContent: String = ""
) {
    val qrBitmap = remember(qrContent) { QRCodeGenerator.generate(qrContent, size = 512) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large) // 16dp
            .background(CardSurface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ENTRY TICKET",
                color = NeonPink,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "LPR-Edge Garage",
                color = PrimaryText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // QR Code Slot
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR code for ticket",
                        modifier = Modifier.size(150.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White)
                    )
                }
            }
        }

        // Dashed Separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 16.dp)
                .drawBehind {
                    drawLine(
                        color = SecondaryText.copy(alpha = 0.5f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
        )

        // Bottom Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            PlateDisplay(numbers = plateNumbers, letters = plateLetters)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Details Grid Row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TicketDetailItem(
                    icon = Icons.Default.LocationOn,
                    label = "ZONE",
                    value = zone,
                    modifier = Modifier.weight(1f)
                )
                TicketDetailItem(
                    icon = Icons.Default.AccessTime,
                    label = "ENTRY TIME",
                    value = entryTime,
                    modifier = Modifier.weight(1f)
                )
                TicketDetailItem(
                    icon = Icons.Default.DateRange,
                    label = "DATE",
                    value = date,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TicketDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonPink,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = SecondaryText,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = PrimaryText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun TicketCardPreview() {
    GraduationProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TicketCard(
                plateNumbers = "1234",
                plateLetters = "أ ب ج",
                zone = "Zone A",
                entryTime = "09:45 AM",
                date = "Oct 24, 2023",
                qrContent = "session-id-123"
            )
        }
    }
}