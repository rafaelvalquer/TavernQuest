package com.luminor.tavernquest.domain

import com.luminor.tavernquest.core.util.TavernInviteCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TavernInviteCodeTest {
    @Test fun codeIsStableShortAndHumanFriendly() {
        val first = TavernInviteCode.fromId("8f7a3b2d-7a68-4b46-b74d-2b8d2c2f3f11")
        assertEquals(first, TavernInviteCode.fromId("8f7a3b2d-7a68-4b46-b74d-2b8d2c2f3f11"))
        assertEquals(6, first.length)
        assertTrue(first.all { it in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" })
    }
}
