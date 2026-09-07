package com.luminor.tavernquest.data.local.database.dao
import androidx.room.*; import com.luminor.tavernquest.data.local.database.entity.HeroEntity; import kotlinx.coroutines.flow.Flow
@Dao interface HeroDao{@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insert(v:HeroEntity);@Query("SELECT * FROM hero LIMIT 1") suspend fun getHero():HeroEntity?;@Query("SELECT * FROM hero LIMIT 1") fun observeHero():Flow<HeroEntity?>;@Query("UPDATE hero SET totalXp=totalXp+:amount WHERE id=:id") suspend fun updateXp(id:String,amount:Int);@Query("DELETE FROM hero") suspend fun deleteAll()}
