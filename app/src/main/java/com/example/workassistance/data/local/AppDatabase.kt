package com.example.workassistance.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.workassistance.data.local.dao.AttendanceDao
import com.example.workassistance.data.local.dao.ProofOfWorkDao
import com.example.workassistance.data.local.dao.SiteTaskDao
import com.example.workassistance.data.local.dao.SiteRequestDao
import com.example.workassistance.data.local.entity.AttendanceRecord
import com.example.workassistance.data.local.entity.ProofOfWorkPhoto
import com.example.workassistance.data.local.entity.SiteTaskEntity
import com.example.workassistance.data.local.entity.SiteRequestEntity

@Database(
    entities = [
        AttendanceRecord::class,
        ProofOfWorkPhoto::class,
        SiteTaskEntity::class,
        SiteRequestEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao

    abstract fun proofOfWorkDao(): ProofOfWorkDao

    abstract fun siteTaskDao(): SiteTaskDao

    abstract fun siteRequestDao(): SiteRequestDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new tables; keep existing attendance_records intact.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS proof_of_work_photos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        siteId TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        note TEXT,
                        createdAt INTEGER NOT NULL,
                        synced INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS site_requests (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        siteId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        quantity INTEGER,
                        createdAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        synced INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add missing column to proof_of_work_photos.
                db.execSQL("ALTER TABLE proof_of_work_photos ADD COLUMN taskId TEXT")

                // Add site_tasks table.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS site_tasks (
                        taskId TEXT NOT NULL,
                        siteId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        dueAt INTEGER,
                        priority TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        completedAt INTEGER,
                        synced INTEGER NOT NULL,
                        PRIMARY KEY(taskId)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "work_assistance.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
