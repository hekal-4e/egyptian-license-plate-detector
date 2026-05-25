package com.depi.graduationproject.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GreenBg
import com.depi.graduationproject.core.theme.InputBlack
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.MutedText
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PlateMono
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.theme.TinyCaps
import com.depi.graduationproject.core.theme.WarningAmber

@Composable
fun PlateVerificationSheet(
    plateNumbers: String,
    plateLetters: String,
    croppedPlateImageBytes: ByteArray?,
    isVerified: Boolean,
    isConfirmEnabled: Boolean,
    duplicateError: String?,
    onNumbersChanged: (String) -> Unit,
    onLettersChanged: (String) -> Unit,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val croppedBitmap = remember(croppedPlateImageBytes) {
        croppedPlateImageBytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PanelSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MutedText.copy(alpha = 0.4f))
        )

        Text(
            text = "Scan Verification",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )

        Text(
            text = "Review and confirm plate detection",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "DETECTED IMAGE",
            style = TinyCaps,
            color = MutedText,
            modifier = Modifier.align(Alignment.Start)
        )

        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(com.depi.graduationproject.core.theme.AppBackground)
                .border(1.dp, MutedText.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (croppedBitmap != null) {
                Image(
                    bitmap = croppedBitmap,
                    contentDescription = "Cropped plate image",
                    modifier = Modifier
                        .size(width = 200.dp, height = 80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "Cropped plate preview",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val badgeBackground = if (isVerified) GreenBg else WarningAmber.copy(alpha = 0.2f)
        val badgeColor = if (isVerified) EmeraldGreen else WarningAmber
        val badgeText = if (isVerified) "AI Verified Match" else "Needs Review"

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBackground)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                )
                Text(
                    text = badgeText,
                    style = TinyCaps,
                    color = badgeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PLATE NUMBER",
            style = TinyCaps,
            color = MutedText,
            modifier = Modifier.align(Alignment.Start)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(InputBlack)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditablePlateField(
                    value = plateNumbers,
                    onValueChange = onNumbersChanged,
                    placeholder = "1234",
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MutedText.copy(alpha = 0.3f))
                )
                EditablePlateField(
                    value = plateLetters,
                    onValueChange = onLettersChanged,
                    placeholder = "أ ب ج",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text(
            text = "Tap to edit if incorrect",
            style = TinyCaps,
            color = MutedText
        )

        if (duplicateError != null) {
            Text(
                text = duplicateError,
                style = MaterialTheme.typography.bodySmall,
                color = NeonPink
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Retake",
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            )
            GradientButton(
                text = if (duplicateError != null) "GO TO CHECKOUT" else "Confirm & Save",
                onClick = onConfirm,
                modifier = Modifier.weight(2f),
                enabled = isConfirmEnabled
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EditablePlateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = PlateMono,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = PrimaryText,
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlateMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MutedText.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}
