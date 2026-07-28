package com.lysanderuy.tulogs.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v1 -> v2 bumped the version but nothing in the schema actually changed
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
    }
}

val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE alarms ADD COLUMN daysOfWeek TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE alarms ADD COLUMN date INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE alarms ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE sleep_logs ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE sleep_tags ADD COLUMN userId TEXT NOT NULL DEFAULT ''")

        db.execSQL("DROP INDEX IF EXISTS index_sleep_tags_type")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sleep_tags_userId_type ON sleep_tags (userId, type)")
    }
}
