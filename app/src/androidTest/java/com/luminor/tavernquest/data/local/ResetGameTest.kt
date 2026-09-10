package com.luminor.tavernquest.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.luminor.tavernquest.data.local.database.TavernQuestDatabase
import com.luminor.tavernquest.domain.model.AuthUser
import com.luminor.tavernquest.domain.repository.AuthRepository
import com.luminor.tavernquest.domain.repository.GameSettings
import com.luminor.tavernquest.domain.repository.SettingsRepository
import com.luminor.tavernquest.domain.usecase.settings.ResetGameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResetGameTest {
    @Test fun clearsRoomFromMainThreadBeforeSigningOut() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TavernQuestDatabase::class.java,
        ).build()
        val events = mutableListOf<String>()
        val settings = object : SettingsRepository {
            override fun observe() = flowOf(GameSettings())
            override suspend fun setOnboardingCompleted(v: Boolean) = Unit
            override suspend fun setSound(v: Boolean) = Unit
            override suspend fun setHaptics(v: Boolean) = Unit
            override suspend fun reset() { events.add("reset") }
        }
        val auth = object : AuthRepository {
            override val currentUser = flowOf<AuthUser?>(null)
            override suspend fun loginWithGoogleIdToken(idToken: String): Result<AuthUser> = error("Unexpected login")
            override suspend fun ensureAccount(): Result<Unit> = error("Unexpected remote account mutation")
            override fun logout() { events.add("logout") }
        }
        try {
            withContext(Dispatchers.IO) {
                db.openHelper.writableDatabase.execSQL("INSERT INTO tavern(id,name,emblem,createdAt,description,isPrivate) VALUES ('cache','Cache','WOLF',1,'',1)")
            }
            // Reproduce the ViewModel dispatcher; Room's main-thread checks stay enabled.
            withContext(Dispatchers.Main) { ResetGameUseCase(db, settings, auth)() }
            withContext(Dispatchers.IO) {
                db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM tavern").use {
                    it.moveToFirst()
                    assertEquals(0, it.getInt(0))
                }
            }
            assertEquals(listOf("reset", "logout"), events)
        } finally {
            withContext(Dispatchers.IO) { db.close() }
        }
    }
}
