package com.luminor.tavernquest.domain.usecase.sync

import com.luminor.tavernquest.domain.repository.SyncRepository
import javax.inject.Inject

/** Backend-independent queue coordinator. Firebase validation calls markValidated after its transaction succeeds. */
class SyncPendingCheckInsUseCase @Inject constructor(private val sync: SyncRepository) {
    suspend operator fun invoke(): Int = sync.pendingCount()
}
