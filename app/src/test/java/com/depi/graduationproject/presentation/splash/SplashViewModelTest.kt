package com.depi.graduationproject.presentation.splash

import com.depi.graduationproject.data.mlkit.TFLiteModelManager
import com.depi.graduationproject.testutils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(dispatcher)

    @Test
    fun initialize_modelReady_setsReadyState() = runTest(dispatcher) {
        val readyFlow = MutableStateFlow(false)
        val errorFlow = MutableStateFlow<String?>(null)
        val manager = mockk<TFLiteModelManager>()

        every { manager.isReady } returns readyFlow
        every { manager.error } returns errorFlow
        coEvery { manager.initializeModels() } coAnswers { readyFlow.value = true }

        val viewModel = SplashViewModel(manager)

        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isReady)
        assertEquals(null, state.error)
    }

    @Test
    fun initialize_modelError_setsErrorState() = runTest(dispatcher) {
        val readyFlow = MutableStateFlow(false)
        val errorFlow = MutableStateFlow<String?>(null)
        val manager = mockk<TFLiteModelManager>()

        every { manager.isReady } returns readyFlow
        every { manager.error } returns errorFlow
        coEvery { manager.initializeModels() } coAnswers { errorFlow.value = "Init failed" }

        val viewModel = SplashViewModel(manager)

        advanceTimeBy(300)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.error != null)
        assertEquals(false, state.isReady)
    }
}
