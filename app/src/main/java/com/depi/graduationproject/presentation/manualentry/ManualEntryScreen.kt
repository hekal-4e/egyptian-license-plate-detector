package com.depi.graduationproject.presentation.manualentry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.LicensePlateTextField
import com.depi.graduationproject.presentation.components.SecondaryButton

@Composable
fun ManualEntryScreen(
    viewModel: ManualEntryViewModel = hiltViewModel(),
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    val numbers by viewModel.numbers.collectAsState()
    val letters by viewModel.letters.collectAsState()
    val isValid by viewModel.isValid.collectAsState()

    ManualEntryScreenContent(
        numbers = numbers,
        onNumbersChange = viewModel::onNumbersChange,
        letters = letters,
        onLettersChange = viewModel::onLettersChange,
        isValid = isValid,
        onConfirm = { onConfirm(viewModel.getCombinedPlate()) },
        onCancel = onCancel
    )
}

@Composable
fun ManualEntryScreenContent(
    numbers: String,
    onNumbersChange: (String) -> Unit,
    letters: String,
    onLettersChange: (String) -> Unit,
    isValid: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Manual Entry",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Enter the license plate details manually if the scanner failed.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LicensePlateTextField(
            numbers = numbers,
            onNumbersChange = onNumbersChange,
            letters = letters,
            onLettersChange = onLettersChange,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        GradientButton(
            text = "Confirm Check-In",
            onClick = onConfirm,
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        SecondaryButton(
            text = "Cancel",
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun ManualEntryScreenPreview() {
    GraduationProjectTheme {
        ManualEntryScreenContent(
            numbers = "123",
            onNumbersChange = {},
            letters = "أ ب ج",
            onLettersChange = {},
            isValid = true,
            onConfirm = {},
            onCancel = {}
        )
    }
}
