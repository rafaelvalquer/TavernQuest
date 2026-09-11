package com.luminor.tavernquest.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.luminor.tavernquest.data.local.database.DatabaseMigrations
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2To7Test {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), TavernQuestDatabase::class.java)

    @Test fun preservesVersionTwoActivityAndPendingUpload() {
        val name = "migration-2-7"
        helper.createDatabase(name, 2).apply {
            execSQL("INSERT INTO hero VALUES ('hero','Hero','FEMININE','MAGE',120,1000)")
            execSQL("INSERT INTO tavern VALUES ('tavern','Taberna','WOLF',1000)")
            execSQL("INSERT INTO tavern_member VALUES ('member','tavern','hero','OWNER',1000)")
            execSQL("INSERT INTO daily_contract VALUES ('daily','template','hero','2026-09-07','COMPLETED',1000,61000,1000)")
            execSQL("INSERT INTO check_in VALUES ('check','daily','hero','nota','/photo.jpg',61000,'Missão','EXPLORATION',120,1000,60,'2026-09-07','PENDING_SYNC')")
            execSQL("INSERT INTO xp_ledger VALUES ('xp','hero',120,'QUEST','daily',61000)")
            execSQL("INSERT INTO user_stats VALUES ('hero',1,120,1,60)")
            execSQL("INSERT INTO user_activity_day VALUES ('hero','2026-09-07',1,120,60,'EXPLORATION','/photo.jpg')")
            execSQL("INSERT INTO pending_sync VALUES ('check',61000,2,'offline')")
            close()
        }
        helper.runMigrationsAndValidate(name, 7, true,
            DatabaseMigrations.MIGRATION_2_3, DatabaseMigrations.MIGRATION_3_4,
            DatabaseMigrations.MIGRATION_4_5, DatabaseMigrations.MIGRATION_5_6,
            DatabaseMigrations.MIGRATION_6_7,
        ).apply {
            query("SELECT userId,totalXp FROM hero").use {
                assertTrue(it.moveToFirst()); assertEquals("hero", it.getString(0)); assertEquals(120, it.getInt(1))
            }
            query("SELECT missionId,durationSeconds,syncStatus,proofPhotoPath FROM check_in").use {
                assertTrue(it.moveToFirst()); assertEquals("template", it.getString(0))
                assertEquals(60, it.getInt(1)); assertEquals("PENDING_SYNC", it.getString(2)); assertEquals("/photo.jpg", it.getString(3))
            }
            query("SELECT attempts,lastError FROM pending_sync WHERE checkInId='check'").use {
                assertTrue(it.moveToFirst()); assertEquals(2, it.getInt(0)); assertEquals("offline", it.getString(1))
            }
            query("SELECT totalXp,activeSeconds,currentStreak,longestStreak FROM user_stats").use {
                assertTrue(it.moveToFirst()); assertEquals(120, it.getInt(0)); assertEquals(60, it.getInt(1))
                assertEquals(0, it.getInt(2)); assertEquals(0, it.getInt(3))
            }
            query("SELECT totalXp,activeSeconds FROM user_activity_day").use {
                assertTrue(it.moveToFirst()); assertEquals(120, it.getInt(0)); assertEquals(60, it.getInt(1))
            }
            query("SELECT amount FROM xp_ledger").use { assertTrue(it.moveToFirst()); assertEquals(120, it.getInt(0)) }
            query("SELECT checkInId FROM tavern_feed WHERE tavernId='tavern'").use {
                assertTrue(it.moveToFirst()); assertEquals("check", it.getString(0))
            }
            close()
        }
    }
}
