package com.example.excuseencyclopedia.data

import kotlinx.coroutines.flow.Flow

class OfflineExcuseRepository(private val excuseDao: ExcuseDao) : ExcuseRepository {

    override fun getAllExcusesStream(): Flow<List<Excuse>> = excuseDao.getAllExcuses()

    override fun getExcusesByDateStream(date: String): Flow<List<Excuse>> = excuseDao.getExcusesByDate(date)

    override suspend fun insertExcuse(excuse: Excuse) = excuseDao.insertExcuse(excuse)

    override suspend fun deleteExcuse(excuse: Excuse) = excuseDao.deleteExcuse(excuse)

    override suspend fun updateExcuse(excuse: Excuse) = excuseDao.updateExcuse(excuse)
}