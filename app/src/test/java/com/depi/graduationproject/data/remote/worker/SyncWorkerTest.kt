package com.depi.graduationproject.data.remote.worker

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.domain.model.SyncStatus
import com.depi.graduationproject.domain.repository.ICloudRepository
import com.depi.graduationproject.domain.repository.IParkingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SyncWorkerTest {

    @Test
    fun doWork_noUnsyncedSessions_returnsSuccess() = runTest {
        val parkingRepository = mockk<IParkingRepository>()
        val cloudRepository = mockk<ICloudRepository>()
        coEvery { parkingRepository.getUnsyncedSessions() } returns emptyList()

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        val worker = buildWorker(parkingRepository, cloudRepository)

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { cloudRepository.uploadSessions(any()) }
    }

    @Test
    fun doWork_uploadFailure_returnsRetry() = runTest {
        val parkingRepository = mockk<IParkingRepository>()
        val cloudRepository = mockk<ICloudRepository>()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        val session = ParkingSession(
            id = "1",
            licensePlate = "ABC123",
            zoneId = "A",
            entryTime = 100L,
            hourlyRateApplied = 10.0,
            status = SessionStatus.ACTIVE,
            syncStatus = SyncStatus.PENDING
        )

        coEvery { parkingRepository.getUnsyncedSessions() } returns listOf(session)
        coEvery { cloudRepository.uploadSessions(any()) } returns Result.failure(Exception("fail"))

        val worker = buildWorker(parkingRepository, cloudRepository)

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    private fun buildWorker(
        parkingRepository: IParkingRepository,
        cloudRepository: ICloudRepository
    ): SyncWorker {
        val context = mockk<Context>(relaxed = true)
        val params = mockk<androidx.work.WorkerParameters>(relaxed = true)
        return SyncWorker(context, params, parkingRepository, cloudRepository)
    }
}