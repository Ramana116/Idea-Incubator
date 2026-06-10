package com.example.data.repository

import com.example.data.local.StartupIdeaDao
import com.example.data.local.StartupIdeaEntity
import com.example.data.model.StartupIdea
import com.example.data.model.StartupReport
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class StartupRepository(private val startupIdeaDao: StartupIdeaDao) {
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(StartupReport::class.java)

    val allStartupIdeas: Flow<List<StartupIdea>> = startupIdeaDao.getAllStartupIdeas().map { entities ->
        entities.map { entity ->
            val report = entity.reportJson?.let { json ->
                try {
                    adapter.fromJson(json)
                } catch (e: Exception) {
                    null
                }
            }
            StartupIdea(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                industry = entity.industry,
                targetAudience = entity.targetAudience,
                timestamp = entity.timestamp,
                report = report
            )
        }
    }.flowOn(Dispatchers.Default)

    suspend fun insertIdea(idea: StartupIdea) {
        val json = idea.report?.let { report ->
            try {
                adapter.toJson(report)
            } catch (e: Exception) {
                null
            }
        }
        val entity = StartupIdeaEntity(
            id = idea.id,
            name = idea.name,
            description = idea.description,
            industry = idea.industry,
            targetAudience = idea.targetAudience,
            timestamp = idea.timestamp,
            reportJson = json
        )
        startupIdeaDao.insertStartupIdea(entity)
    }

    suspend fun deleteIdeaById(id: Int) {
        startupIdeaDao.deleteStartupIdeaById(id)
    }
}
