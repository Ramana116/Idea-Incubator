package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StartupIdeaDao {
    @Query("SELECT * FROM startup_ideas ORDER BY timestamp DESC")
    fun getAllStartupIdeas(): Flow<List<StartupIdeaEntity>>

    @Query("SELECT * FROM startup_ideas WHERE id = :id LIMIT 1")
    suspend fun getStartupIdeaById(id: Int): StartupIdeaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStartupIdea(idea: StartupIdeaEntity): Long

    @Query("DELETE FROM startup_ideas WHERE id = :id")
    suspend fun deleteStartupIdeaById(id: Int)
}
