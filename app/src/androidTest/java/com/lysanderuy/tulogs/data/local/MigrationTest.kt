package com.lysanderuy.tulogs.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TuLogsDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesExistingRows() {
        helper.createDatabase(testDb, 1).apply {
            execSQL("INSERT INTO alarms (id, hour, minute, isEnabled, label) VALUES (1, 7, 30, 1, 'Wake up')")
            execSQL("INSERT INTO sleep_tags (id, uid, type) VALUES (1, 'tag-uid-1', 'BEDTIME')")
            execSQL("INSERT INTO sleep_logs (id, bedtimeTimestamp, wakeTimestamp, screenOffTimestamp, firstScreenOnTimestamp) VALUES (1, 1000, 2000, 1500, 1600)")
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        db.query("SELECT hour, minute, label FROM alarms WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(7, cursor.getInt(0))
            assertEquals(30, cursor.getInt(1))
            assertEquals("Wake up", cursor.getString(2))
        }
        db.query("SELECT uid, type FROM sleep_tags WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("tag-uid-1", cursor.getString(0))
            assertEquals("BEDTIME", cursor.getString(1))
        }
        db.query("SELECT bedtimeTimestamp, wakeTimestamp FROM sleep_logs WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(1000L, cursor.getLong(0))
            assertEquals(2000L, cursor.getLong(1))
        }
    }

    @Test
    fun migrate2To4_addsAlarmColumnsWithDefaultsAndPreservesData() {
        helper.createDatabase(testDb, 2).apply {
            execSQL("INSERT INTO alarms (id, hour, minute, isEnabled, label) VALUES (1, 6, 45, 1, 'Old alarm')")
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 4, true, MIGRATION_2_4)

        db.query("SELECT hour, minute, label, daysOfWeek, date FROM alarms WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(6, cursor.getInt(0))
            assertEquals(45, cursor.getInt(1))
            assertEquals("Old alarm", cursor.getString(2))
            assertEquals("", cursor.getString(3))
            assertEquals(0L, cursor.getLong(4))
        }
    }

    @Test
    fun migrate4To5_addsUserIdColumnsAndRebuildsSleepTagIndex() {
        helper.createDatabase(testDb, 4).apply {
            execSQL("INSERT INTO alarms (id, hour, minute, isEnabled, label, daysOfWeek, date) VALUES (1, 8, 0, 1, 'Legacy', '', 0)")
            execSQL("INSERT INTO sleep_tags (id, uid, type) VALUES (1, 'tag-uid-1', 'BEDTIME')")
            execSQL("INSERT INTO sleep_tags (id, uid, type) VALUES (2, 'tag-uid-2', 'WAKE')")
            execSQL("INSERT INTO sleep_logs (id, bedtimeTimestamp) VALUES (1, 5000)")
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 5, true, MIGRATION_4_5)

        db.query("SELECT userId, hour FROM alarms WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
            assertEquals(8, cursor.getInt(1))
        }
        db.query("SELECT userId, bedtimeTimestamp FROM sleep_logs WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
            assertEquals(5000L, cursor.getLong(1))
        }
        db.query("SELECT COUNT(*) FROM sleep_tags WHERE userId = '' AND type = 'BEDTIME'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        // index is now (userId, type), so a second BEDTIME for another user should insert fine
        db.execSQL("INSERT INTO sleep_tags (id, userId, uid, type) VALUES (3, 'real-user', 'tag-uid-3', 'BEDTIME')")
        db.query("SELECT COUNT(*) FROM sleep_tags WHERE type = 'BEDTIME'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals(2, cursor.getInt(0))
        }
    }

    @Test
    fun migrateAll_1To5_preservesDataThroughFullChain() {
        helper.createDatabase(testDb, 1).apply {
            execSQL("INSERT INTO alarms (id, hour, minute, isEnabled, label) VALUES (1, 7, 0, 1, 'Full chain')")
            execSQL("INSERT INTO sleep_tags (id, uid, type) VALUES (1, 'tag-uid-1', 'BEDTIME')")
            execSQL("INSERT INTO sleep_logs (id, bedtimeTimestamp) VALUES (1, 9000)")
            close()
        }

        val db = helper.runMigrationsAndValidate(
            testDb, 5, true,
            MIGRATION_1_2, MIGRATION_2_4, MIGRATION_4_5
        )

        db.query("SELECT userId, label, daysOfWeek, date FROM alarms WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
            assertEquals("Full chain", cursor.getString(1))
            assertEquals("", cursor.getString(2))
            assertEquals(0L, cursor.getLong(3))
        }
        db.query("SELECT userId, uid, type FROM sleep_tags WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
            assertEquals("tag-uid-1", cursor.getString(1))
            assertEquals("BEDTIME", cursor.getString(2))
        }
        db.query("SELECT userId, bedtimeTimestamp FROM sleep_logs WHERE id = 1").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
            assertEquals(9000L, cursor.getLong(1))
        }

        // sanity check: make sure Room itself is happy opening the migrated db
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val roomDb = Room.databaseBuilder(context, TuLogsDatabase::class.java, testDb)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_4, MIGRATION_4_5)
            .build()
        roomDb.openHelper.writableDatabase
        roomDb.close()
    }
}
