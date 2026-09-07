package com.luminor.tavernquest.data.local.database.dao
import androidx.room.*; import com.luminor.tavernquest.data.local.database.entity.XpLedgerEntity; import kotlinx.coroutines.flow.Flow
@Dao interface XpLedgerDao{@Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insert(v:XpLedgerEntity):Long;@Query("SELECT COALESCE(SUM(amount),0) FROM xp_ledger WHERE heroId=:id") fun observeHeroXp(id:String):Flow<Int>;@Query("SELECT COALESCE(SUM(amount),0) FROM xp_ledger WHERE heroId=:id") suspend fun getTotalXp(id:String):Int;@Query("DELETE FROM xp_ledger") suspend fun deleteAll()}
