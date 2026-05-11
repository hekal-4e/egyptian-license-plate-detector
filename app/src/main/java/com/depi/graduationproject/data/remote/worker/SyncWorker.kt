package com.depi.graduationproject.data.remote.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.depi.graduationproject.domain.repository.ICloudRepository
import com.depi.graduationproject.domain.repository.IParkingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val parkingRepository: IParkingRepository,
    private val cloudRepository: ICloudRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedSessions = parkingRepository.getUnsyncedSessions()

            if (unsyncedSessions.isEmpty()) {
                return Result.success()
            }

            val uploadResult = cloudRepository.uploadSessions(unsyncedSessions)

            if (uploadResult.isSuccess) {
                val ids = unsyncedSessions.map { it.id }
                parkingRepository.markSynced(ids)
                Log.d(TAG, "Successfully synced ${ids.size} sessions")
                Result.success()
            } else {
                Log.e(TAG, "Failed to upload sessions to Supabase", uploadResult.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync worker execution", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}