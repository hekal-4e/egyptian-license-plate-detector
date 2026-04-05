package com.depi.graduationproject.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlateResultBottomSheet(
    plateNumber: String,
    plateImage: Bitmap?,
    onSave: (String) -> Unit,
    onRetry: () -> Unit
) {
    var editedText by remember(plateNumber) { mutableStateOf(plateNumber) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "License Plate Detected",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Display Cropped Image (if available)
        if (plateImage != null) {
            Image(
                bitmap = plateImage.asImageBitmap(),
                contentDescription = "Cropped Plate",
                modifier = Modifier
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth(0.6f),
                contentScale = ContentScale.FillBounds
            )
        }

        // Editable Text Field
        OutlinedTextField(
            value = editedText,
            onValueChange = { editedText = it },
            label = { Text("Plate Number") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp // Spacing for easier reading
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Retry Button
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Retry")
            }

            // Save Button
            Button(
                onClick = {
                    if (editedText.isNotBlank()) {
                        onSave(editedText) // Save the edited text
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}