package com.luminor.tavernquest.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luminor.tavernquest.core.time.DateProvider
import com.luminor.tavernquest.core.util.UuidProvider
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.data.local.database.entity.*
import com.luminor.tavernquest.data.repository.QuestRepositoryImpl
import com.luminor.tavernquest.data.repository.SyncRepositoryImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CheckInPersistenceTest {
    private lateinit var db: TavernQuestDatabase
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "checkin-test.db"
    private var now = 1_788_800_000_000L
    private val time = object : DateProvider {
        override fun nowMillis() = now
        override fun today() = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    private val ids = object : UuidProvider { override fun newId() = UUID.randomUUID().toString() }
    private fun repository() = QuestRepositoryImpl(db, ids, time)
    private fun open() { db = Room.databaseBuilder(context, TavernQuestDatabase::class.java, name).build() }

    @Before fun setup() = runBlocking {
        context.deleteDatabase(name)
        open()
        db.heroDao().insert(HeroEntity("hero", "Rafael", "MASCULINE", "WARRIOR", 0, now))
        db.contractTemplateDao().insertAll(listOf(ContractTemplateEntity("walk", "Caminhar", "Caminhar 5 km", "EXPLORATION", "EASY", 80, true)))
        db.dailyContractDao().insertAll(listOf(DailyContractEntity("mission", "walk", "hero", time.today().toString(), "ACCEPTED", now, null)))
    }
    @After fun teardown() { db.close(); context.deleteDatabase(name) }

    @Test fun concurrentCompletionAwardsXpAndQueuesExactlyOnce() = runBlocking {
        assertTrue(repository().start("mission", "hero"))
        now += 3_120_000
        val results = (1..8).map { async(Dispatchers.IO) { repository().complete("mission", "hero", "Caminhada", null) } }.awaitAll()
        assertEquals(1, results.count { it })
        assertEquals(80, db.heroDao().getHero()!!.totalXp)
        assertEquals(80, db.xpLedgerDao().getTotalXp("hero"))
        assertEquals(1, db.questCompletionDao().count())
        assertEquals(1, db.activityDao().pending().size)
        val checkIn = db.questCompletionDao().observeAll().first().single()
        assertEquals(3120L, checkIn.durationSeconds)
        assertEquals("PENDING_SYNC", checkIn.syncStatus)
        assertEquals("Caminhar", checkIn.title)
        assertEquals("walk", checkIn.missionId)
        assertEquals(UserStatsEntity("hero", 1, 80, 1, 3120, 1, 1), db.activityDao().observeStats("hero").first())
    }

    @Test fun timerSurvivesDatabaseReopenAndDoesNotRestart() = runBlocking {
        val started = now
        assertTrue(repository().start("mission", "hero"))
        db.close(); open()
        now += 60_000
        assertTrue(repository().start("mission", "hero"))
        assertEquals(started, db.dailyContractDao().getById("mission")!!.contract.startedAt)
        db.dailyContractDao().expireBefore(time.today().plusDays(1).toString())
        assertTrue(repository().complete("mission", "hero", "", null))
        assertEquals(60L, db.questCompletionDao().observeAll().first().single().durationSeconds)
    }

    @Test fun cannotCompleteWithoutStartingOrAsAnotherHero() = runBlocking {
        assertFalse(repository().complete("mission", "hero", "", null))
        assertFalse(repository().start("mission", "intruder"))
        assertTrue(repository().start("mission", "hero"))
        assertFalse(repository().complete("mission", "intruder", "", null))
        assertEquals(0, db.questCompletionDao().count())
        assertEquals(0, db.xpLedgerDao().getTotalXp("hero"))
    }

    @Test fun queueFailureRollsBackCheckInXpStatsAndStatus() = runBlocking {
        assertTrue(repository().start("mission", "hero"))
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_queue BEFORE INSERT ON pending_sync BEGIN SELECT RAISE(ABORT, 'test failure'); END")
        try {
            repository().complete("mission", "hero", "", null)
            fail("Expected database failure")
        } catch (_: android.database.sqlite.SQLiteException) { }
        assertEquals(0, db.questCompletionDao().count())
        assertEquals(0, db.heroDao().getHero()!!.totalXp)
        assertEquals(0, db.xpLedgerDao().getTotalXp("hero"))
        assertNull(db.activityDao().observeStats("hero").first())
        assertEquals("ACCEPTED", db.dailyContractDao().getById("mission")!!.contract.status)
    }

    @Test fun serverValidationReconcilesOfficialXpAndClearsQueue() = runBlocking {
        assertTrue(repository().start("mission", "hero"))
        now += 60_000
        assertTrue(repository().complete("mission", "hero", "", null))
        val checkIn = db.questCompletionDao().observeAll().first().single()
        SyncRepositoryImpl(db).markValidated(checkIn.id, 100)
        assertEquals(100, db.heroDao().getHero()!!.totalXp)
        assertEquals(100, db.questCompletionDao().observeAll().first().single().let { it.xpEarned })
        assertEquals("VALIDATED", db.questCompletionDao().observeAll().first().single().syncStatus)
        assertEquals(0, db.activityDao().countPending())
        assertEquals(100, db.activityDao().observeStats("hero").first()!!.totalXp)
    }

    @Test fun serverRejectionRemovesProvisionalXpAndRefreshesStats() = runBlocking {
        assertTrue(repository().start("mission", "hero"))
        now += 60_000
        assertTrue(repository().complete("mission", "hero", "", null))
        val checkIn = db.questCompletionDao().observeAll().first().single()
        SyncRepositoryImpl(db).markRejected(checkIn.id, "invalid")
        assertEquals(0, db.heroDao().getHero()!!.totalXp)
        assertEquals("REJECTED", db.questCompletionDao().observeAll().first().single().syncStatus)
        assertEquals(0, db.activityDao().countPending())
        assertEquals(0, db.activityDao().observeStats("hero").first()!!.totalXp)
        assertTrue(db.activityDao().observeMonth("hero", checkIn.activityDate, java.time.LocalDate.parse(checkIn.activityDate).plusDays(1).toString()).first().isEmpty())
    }

    @Test fun oneCheckInIsPublishedToEveryCurrentTavernWithoutDuplicatingXp() = runBlocking {
        db.tavernDao().insert(TavernEntity("tavern-a", "Alcateia", "WOLF", now))
        db.tavernDao().insert(TavernEntity("tavern-b", "Guerreiros", "BEAR", now))
        db.tavernMemberDao().insert(TavernMemberEntity("member-a", "tavern-a", "hero", "OWNER", now))
        db.tavernMemberDao().insert(TavernMemberEntity("member-b", "tavern-b", "hero", "MEMBER", now))
        assertTrue(repository().start("mission", "hero"))
        now += 60_000
        assertTrue(repository().complete("mission", "hero", "", null))
        val checkIn = db.questCompletionDao().observeAll().first().single()
        assertEquals(1, db.questCompletionDao().count())
        assertEquals(80, db.heroDao().getHero()!!.totalXp)
        assertEquals(1, db.tavernFeedDao().observe("tavern-a").first().size)
        assertEquals(1, db.tavernFeedDao().observe("tavern-b").first().size)
        assertEquals(checkIn.id, db.tavernFeedDao().observe("tavern-a").first().single().id)
    }
}
