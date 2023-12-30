package com.choi.sensorproject.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.choi.sensorproject.room.entity.AppInfoEntity

@Dao
interface AppInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppInfo(
        appInfoEntity: AppInfoEntity
    )

    @Query("SELECT * FROM app_info WHERE app_name =:appName")
    fun getAppInfo(appName: String) : AppInfoEntity
}