package com.luminor.tavernquest.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseTavernRemoteRepositoryTest {
    private val repository = FirebaseTavernRemoteRepository(firestore = null, auth = null)

    @Test
    fun remoteObserversReturnEmptyDataWhenFirebaseIsUnavailable() = runBlocking {
        assertTrue(repository.observeTaverns().first().isEmpty())
        assertTrue(repository.observeMembers("tavern-1").first().isEmpty())
        assertTrue(repository.observeFeed("tavern-1").first().isEmpty())
        assertTrue(repository.observeRanking("tavern-1", null).first().isEmpty())
    }
}
