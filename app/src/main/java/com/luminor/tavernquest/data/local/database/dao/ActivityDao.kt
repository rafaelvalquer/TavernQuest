package com.luminor.tavernquest.data.local.database.dao

import androidx.room.*
import com.luminor.tavernquest.data.local.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM user_stats WHERE heroId=:heroId")
    fun observeStats(heroId: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_activity_day WHERE heroId=:heroId AND date>=:from AND date<:until ORDER BY date")
    fun observeMonth(heroId: String, from: String, until: String): Flow<List<UserActivityDayEntity>>

    @Query("SELECT * FROM check_in WHERE heroId=:heroId AND activityDate=:date ORDER BY completedAt DESC")
    fun observeDay(heroId: String, date: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM pending_sync ORDER BY createdAt LIMIT :limit")
    suspend fun pending(limit: Int = 50): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enqueue(value: PendingSyncEntity)

    @Query("""INSERT OR REPLACE INTO user_activity_day
        SELECT heroId, activityDate, COUNT(*), SUM(xpEarned), SUM(durationSeconds),
        (SELECT c.category FROM check_in c WHERE c.heroId=:heroId AND c.activityDate=:date ORDER BY c.completedAt DESC, c.id LIMIT 1),
        (SELECT c.proofPhotoPath FROM check_in c WHERE c.heroId=:heroId AND c.activityDate=:date AND c.proofPhotoPath IS NOT NULL ORDER BY c.completedAt DESC, c.id LIMIT 1)
        FROM check_in WHERE heroId=:heroId AND activityDate=:date AND syncStatus!='REJECTED' GROUP BY heroId, activityDate""")
    suspend fun refreshDay(heroId: String, date: String)

    @Query("""INSERT OR REPLACE INTO user_stats
        SELECT :heroId, COUNT(*), COALESCE(SUM(xpEarned),0), COUNT(DISTINCT activityDate), COALESCE(SUM(durationSeconds),0)
        FROM check_in WHERE heroId=:heroId AND syncStatus!='REJECTED'""")
    suspend fun refreshStats(heroId: String)
}
