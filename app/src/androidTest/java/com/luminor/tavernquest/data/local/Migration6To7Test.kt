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
class Migration6To7Test {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), TavernQuestDatabase::class.java)

    @Test fun preservesTavernAndAddsPrivacyFields() {
        val name = "migration-6-7"
        helper.createDatabase(name, 6).apply {
            execSQL("INSERT INTO tavern(id,name,emblem,createdAt) VALUES ('tavern','Alcateia','WOLF',1000)")
            close()
        }
        helper.runMigrationsAndValidate(name, 7, true, DatabaseMigrations.MIGRATION_6_7).apply {
            query("SELECT name,description,isPrivate FROM tavern WHERE id='tavern'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Alcateia", cursor.getString(0))
                assertEquals("", cursor.getString(1))
                assertEquals(1, cursor.getInt(2))
            }
            close()
        }
    }
}
