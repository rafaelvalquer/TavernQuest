package com.luminor.tavernquest.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luminor.tavernquest.data.local.database.entity.CheckInEntity
import com.luminor.tavernquest.data.local.database.entity.TavernFeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TavernFeedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(values: List<TavernFeedEntity>)

    @Query("""
        SELECT c.* FROM check_in c
        INNER JOIN tavern_feed f ON f.checkInId = c.id
        WHERE f.tavernId = :tavernId AND c.syncStatus != 'REJECTED'
        ORDER BY f.publishedAt DESC
    """)
    fun observe(tavernId: String): Flow<List<CheckInEntity>>

    @Query("DELETE FROM tavern_feed")
    suspend fun deleteAll()
}
