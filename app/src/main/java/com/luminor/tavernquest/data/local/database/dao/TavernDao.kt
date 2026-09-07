package com.luminor.tavernquest.data.local.database.dao
import androidx.room.*; import com.luminor.tavernquest.data.local.database.entity.TavernEntity; import kotlinx.coroutines.flow.Flow
@Dao interface TavernDao{@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insert(v:TavernEntity);@Query("SELECT * FROM tavern LIMIT 1") suspend fun getTavern():TavernEntity?;@Query("SELECT * FROM tavern LIMIT 1") fun observeTavern():Flow<TavernEntity?>;@Query("DELETE FROM tavern") suspend fun deleteAll()}
