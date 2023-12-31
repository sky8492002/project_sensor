package com.choi.sensorproject.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.choi.sensorproject.room.entity.AppInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppInfo(
        appInfoEntity: AppInfoEntity
    )

    @Query("SELECT * FROM app_info WHERE app_name =:appName")
    fun getAppInfo(appName: String) : List<AppInfoEntity>

    @Query("SELECT * FROM app_info")
    fun getAllAppInfos() : Flow<List<AppInfoEntity>>
}