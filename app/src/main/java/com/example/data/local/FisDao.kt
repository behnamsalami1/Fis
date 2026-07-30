package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FisDao {

    // Project Operations
    @Query("SELECT * FROM projects ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectByIdDirect(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // Financial Data Operations
    @Query("SELECT * FROM financial_data WHERE projectId = :projectId LIMIT 1")
    fun getFinancialDataByProject(projectId: Long): Flow<FinancialDataEntity?>

    @Query("SELECT * FROM financial_data WHERE projectId = :projectId LIMIT 1")
    suspend fun getFinancialDataByProjectDirect(projectId: Long): FinancialDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialData(data: FinancialDataEntity): Long

    // Calculation Results Operations
    @Query("SELECT * FROM calculation_results WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getResultsByProject(projectId: Long): Flow<List<CalculationResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculationResult(result: CalculationResultEntity): Long

    @Query("DELETE FROM calculation_results WHERE projectId = :projectId")
    suspend fun clearResultsForProject(projectId: Long)
}
