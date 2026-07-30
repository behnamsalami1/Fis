package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        FinancialDataEntity::class,
        CalculationResultEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FisDatabase : RoomDatabase() {

    abstract fun fisDao(): FisDao

    companion object {
        @Volatile
        private var INSTANCE: FisDatabase? = null

        fun getDatabase(context: Context): FisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FisDatabase::class.java,
                    "fis_trading_system.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
