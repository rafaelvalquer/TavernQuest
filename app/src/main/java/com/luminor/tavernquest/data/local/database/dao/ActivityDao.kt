package com.luminor.tavernquest.data.local.database.dao

import androidx.room.*
import com.luminor.tavernquest.data.local.database.entity.*
import kotlinx.coroutines.flow.Flow

data class TavernRankingRow(val heroId: String, val xp: Long, val activeDays: Long, val checkIns: Long)

@Dao
interface ActivityDao {
    @Query("""SELECT c.heroId AS heroId, COALESCE(SUM(c.xpEarned),0) AS xp,
        COUNT(DISTINCT c.activityDate) AS activeDays, COUNT(*) AS checkIns
        FROM check_in c INNER JOIN tavern_member m ON m.heroId=c.heroId
        WHERE m.tavernId=:tavernId AND c.completedAt>=m.joinedAt AND c.completedAt>=:periodStart AND c.syncStatus IN ('VALIDATED','SYNCED')
        GROUP BY c.heroId ORDER BY xp DESC, activeDays DESC, checkIns DESC, c.heroId ASC""")
    fun observeTavernRanking(tavernId: String, periodStart: Long): Flow<List<TavernRankingRow>>
    @Query("SELECT * FROM user_stats WHERE heroId=:heroId")
    fun observeStats(heroId: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_activity_day WHERE heroId=:heroId AND date>=:from AND date<:until ORDER BY date")
    fun observeMonth(heroId: String, from: String, until: String): Flow<List<UserActivityDayEntity>>

    @Query("SELECT * FROM check_in WHERE heroId=:heroId AND activityDate=:date ORDER BY completedAt DESC")
    fun observeDay(heroId: String, date: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM pending_sync ORDER BY createdAt LIMIT :limit")
    suspend fun pending(limit: Int = 50): List<PendingSyncEntity>

    @Query("SELECT COUNT(*) FROM pending_sync")
    suspend fun countPending(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enqueue(value: PendingSyncEntity)

    @Query("UPDATE pending_sync SET attempts=attempts+1,lastError=:error WHERE checkInId=:checkInId")
    suspend fun recordSyncFailure(checkInId: String, error: String?)

    @Query("DELETE FROM pending_sync WHERE checkInId=:checkInId")
    suspend fun removePending(checkInId: String)

    @Query("UPDATE check_in SET syncStatus=:status WHERE id=:checkInId")
    suspend fun updateSyncStatus(checkInId: String, status: String)

    @Query("SELECT * FROM check_in WHERE id=:checkInId LIMIT 1")
    suspend fun getCheckIn(checkInId: String): CheckInEntity?

    @Query("UPDATE check_in SET syncStatus=:status,xpEarned=:xp WHERE id=:checkInId")
    suspend fun updateValidated(checkInId: String, status: String, xp: Int)

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
