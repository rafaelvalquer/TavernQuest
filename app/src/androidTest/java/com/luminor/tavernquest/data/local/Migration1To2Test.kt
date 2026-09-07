package com.luminor.tavernquest.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luminor.tavernquest.data.local.database.DatabaseMigrations
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), TavernQuestDatabase::class.java)

    @Test fun preservesHistoryXpAndOwnershipWithoutRepublishingLegacyData() {
        val name = "migration-1-2"
        helper.createDatabase(name, 1).apply {
            execSQL("INSERT INTO hero VALUES ('hero','Rafael','MASCULINE','WARRIOR',80,1000)")
            execSQL("INSERT INTO tavern VALUES ('tavern','Alcateia','WOLF',1000)")
            execSQL("INSERT INTO tavern_member VALUES ('member','tavern','hero','OWNER',1000)")
            execSQL("INSERT INTO contract_template VALUES ('walk','Caminhar','5 km','EXPLORATION','EASY',80,1)")
            execSQL("INSERT INTO daily_contract VALUES ('mission','walk','tavern','2026-09-07','COMPLETED',1000,100000)")
            execSQL("INSERT INTO quest_completion VALUES ('check','mission','hero','nota','/photo.jpg',100000)")
            execSQL("INSERT INTO xp_ledger VALUES ('xp','hero',80,'QUEST','mission',100000)")
            close()
        }
        helper.runMigrationsAndValidate(name, 2, true, DatabaseMigrations.MIGRATION_1_2).apply {
            query("SELECT * FROM check_in").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("check", c.getString(c.getColumnIndexOrThrow("id")))
                assertEquals("Caminhar", c.getString(c.getColumnIndexOrThrow("title")))
                assertEquals("/photo.jpg", c.getString(c.getColumnIndexOrThrow("proofPhotoPath")))
                assertEquals("LOCAL_ONLY", c.getString(c.getColumnIndexOrThrow("syncStatus")))
                assertEquals(0, c.getLong(c.getColumnIndexOrThrow("durationSeconds")))
            }
            query("SELECT heroId FROM daily_contract").use { assertTrue(it.moveToFirst()); assertEquals("hero", it.getString(0)) }
            query("SELECT totalXp,totalCheckIns FROM user_stats").use { assertTrue(it.moveToFirst()); assertEquals(80,it.getInt(0)); assertEquals(1,it.getInt(1)) }
            query("SELECT COUNT(*) FROM pending_sync").use { it.moveToFirst(); assertEquals(0,it.getInt(0)) }
            query("SELECT totalXp FROM hero").use { it.moveToFirst(); assertEquals(80,it.getInt(0)) }
            query("SELECT COUNT(*) FROM xp_ledger").use { it.moveToFirst(); assertEquals(1,it.getInt(0)) }
            close()
        }
    }
}
