package com.luminor.tavernquest.domain.repository

import com.luminor.tavernquest.domain.model.Tavern
import com.luminor.tavernquest.domain.model.TavernMember

interface TavernRemoteRepository {
    suspend fun create(tavern: Tavern, owner: TavernMember): Result<Unit>
    suspend fun findByInviteCode(code: String): Result<Tavern?>
    suspend fun join(tavern: Tavern, member: TavernMember): Result<Unit>
    suspend fun leave(tavernId: String): Result<Unit>
}
