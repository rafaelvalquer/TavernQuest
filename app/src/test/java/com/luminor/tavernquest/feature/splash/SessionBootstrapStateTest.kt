package com.luminor.tavernquest.feature.splash

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionBootstrapStateTest {
    @Test fun `without Firebase session opens login`() {
        assertEquals(SessionBootstrapState.NeedsLogin, sessionStateAfterAccount(userPresent = false, heroPresent = false))
    }

    @Test fun `account without hero opens hero creation`() {
        assertEquals(SessionBootstrapState.NeedsHero, sessionStateAfterAccount(userPresent = true, heroPresent = false))
    }

    @Test fun `account with remote hero opens dashboard`() {
        assertEquals(SessionBootstrapState.Ready, sessionStateAfterAccount(userPresent = true, heroPresent = true))
    }

    @Test fun `account service failure remains recoverable`() {
        assertEquals(
            "A conta Google foi reconhecida, mas não foi possível preparar seu perfil. Tente novamente.",
            sessionBootstrapFailure(IllegalStateException("backend failure")),
        )
    }
}
