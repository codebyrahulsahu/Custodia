package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemberEntity::class,
        DocumentEntity::class,
        SignatureEntity::class,
        MedicalEntryEntity::class,
        DriveBackupEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CustodiaDatabase : RoomDatabase() {

    abstract fun custodiaDao(): CustodiaDao

    companion object {
        @Volatile
        private var INSTANCE: CustodiaDatabase? = null

        fun getDatabase(context: Context): CustodiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CustodiaDatabase::class.java,
                    "custodia_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
