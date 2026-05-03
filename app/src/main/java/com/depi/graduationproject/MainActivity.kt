package com.depi.graduationproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.depi.graduationproject.data.local.AppDataBase
import com.depi.graduationproject.data.mlkit.TFLitePlateAnalyzer
import com.depi.graduationproject.repository.PlateRepository
import com.depi.graduationproject.ui.components.PlateResultBottomSheet
import com.depi.graduationproject.core.navigation.AppNavigation
import com.depi.graduationproject.ui.screens.PermissionDeniedScreen
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.ui.viewmodels.MainViewModel
import com.depi.graduationproject.ui.viewmodels.MainViewModelFactory
import com.depi.graduationproject.presentation.components.CameraPermissionWrapper

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDataBase.invoke(applicationContext)
        val repository = PlateRepository(db.plateDao())
        val analyzer = TFLitePlateAnalyzer(applicationContext)
        val factory = MainViewModelFactory(repository, analyzer)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            GraduationProjectTheme {
                CameraPermissionWrapper(
                    onPermissionDenied = { requestPermission ->
                        PermissionDeniedScreen(onRequestPermission = requestPermission)
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            viewModel.eventFlow.collect { message ->
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    message,
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        val isLoading by viewModel.isLoading.collectAsState()
                        val showDialog by viewModel.showDialog.collectAsState()
                        val result by viewModel.analysisResult.collectAsState()

                        AppNavigation(
                            viewModel = viewModel,
                            onImageCaptured = { bitmap ->
                                viewModel.processImage(bitmap)
                            }
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }

                        if (showDialog && result != null) {
                            val sheetState = rememberModalBottomSheetState()

                            if (!result!!.isSuccess) {
                                Toast.makeText(
                                    this,
                                    "Note: ${result!!.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            ModalBottomSheet(
                                onDismissRequest = { viewModel.dismissDialog() },
                                sheetState = sheetState
                            ) {
                                PlateResultBottomSheet(
                                    plateNumber = result!!.text,
                                    plateImage = result!!.imageBytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) },
                                    onSave = { editedText ->
                                        viewModel.onPlateDetected(editedText)
                                        viewModel.dismissDialog()

                                        Toast.makeText(
                                            this@MainActivity,
                                            "Saved: $editedText",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onRetry = {
                                        viewModel.dismissDialog()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}