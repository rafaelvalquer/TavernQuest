package com.luminor.tavernquest.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.XpLedgerEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class XpLedgerDaoTest {
    private lateinit var database: TavernQuestDatabase

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TavernQuestDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() = database.close()

    @Test fun ignoresRepeatedLedgerSourceAndKeepsSingleXpReward() = runBlocking {
        val dao = database.xpLedgerDao()
        val reward = XpLedgerEntity("ledger-1", "hero-1", 80, "QUEST", "contract-1", 1000L)
        val replay = reward.copy(id = "ledger-2", createdAt = 2000L)

        assertTrue(dao.insert(reward) > 0L)
        assertEquals(-1L, dao.insert(replay))
        assertEquals(80, dao.getTotalXp("hero-1"))
    }
}
