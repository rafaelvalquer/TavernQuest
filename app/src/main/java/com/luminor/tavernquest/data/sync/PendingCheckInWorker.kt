package com.luminor.tavernquest.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.mapper.toCheckIn
import com.luminor.tavernquest.domain.repository.CheckInSyncResult
import com.luminor.tavernquest.domain.repository.CheckInSyncTransport
import com.luminor.tavernquest.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PendingCheckInWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: TavernQuestDatabase,
    private val transport: CheckInSyncTransport,
    private val sync: SyncRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val pending = db.activityDao().pending()
        var retry = false
        pending.forEach { item ->
            val checkIn = db.activityDao().getCheckIn(item.checkInId) ?: run { sync.markFailed(item.checkInId, "Check-in local não encontrado."); return@forEach }
            when (val result = transport.upload(checkIn.toCheckIn())) {
                is CheckInSyncResult.Validated -> sync.markValidated(checkIn.id, result.officialXp)
                is CheckInSyncResult.Rejected -> sync.markRejected(checkIn.id, result.reason)
                is CheckInSyncResult.Retry -> { sync.markFailed(checkIn.id, result.reason); retry = true }
            }
        }
        return if (retry) Result.retry() else Result.success()
    }
}
