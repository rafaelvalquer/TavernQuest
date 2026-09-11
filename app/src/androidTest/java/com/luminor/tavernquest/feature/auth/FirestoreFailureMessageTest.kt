package com.luminor.tavernquest.feature.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreFailureMessageTest {
    @Test fun mapsConnectivityCodesWithoutRelyingOnSdkMessage() {
        listOf(
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
        ).forEach { code ->
            assertEquals(
                "Não foi possível acessar seu perfil no Firebase. Verifique sua internet e toque em Tentar novamente.",
                loginFailureMessage(FirebaseFirestoreException("opaque", code), LoginFailureStage.ACCOUNT_SETUP),
            )
        }
    }
}
