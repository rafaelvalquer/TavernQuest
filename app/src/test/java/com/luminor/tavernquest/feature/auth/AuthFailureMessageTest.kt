package com.luminor.tavernquest.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AuthFailureMessageTest {
    @Test fun `maps missing Google account`() {
        assertEquals(
            "Nenhuma conta Google disponível para entrar. Adicione uma conta Google nas configurações do celular e tente novamente.",
            loginFailureMessage(androidx.credentials.exceptions.NoCredentialException(), LoginFailureStage.GOOGLE_CREDENTIAL),
        )
    }
    @Test fun `maps cancelled Google selection`() { assertEquals("A entrada com Google foi cancelada.", loginFailureMessage(IllegalStateException("cancelled"), LoginFailureStage.GOOGLE_CREDENTIAL)) }
    @Test fun `maps network failure`() { assertEquals("Não foi possível conectar agora. Verifique sua internet e tente novamente.", loginFailureMessage(IOException("network unavailable"), LoginFailureStage.FIREBASE_AUTH)) }
    @Test fun `maps unrecognized APK`() { assertEquals("Este APK não foi reconhecido pelo login Google. Instale a versão mais recente do TavernQuest.", loginFailureMessage(IllegalStateException("DEVELOPER_ERROR"), LoginFailureStage.FIREBASE_AUTH)) }
    @Test fun `keeps account setup failure distinct from sign in`() { assertEquals("A conta Google foi reconhecida, mas não foi possível preparar seu perfil. Tente novamente.", loginFailureMessage(IllegalStateException("backend"), LoginFailureStage.ACCOUNT_SETUP)) }
}
