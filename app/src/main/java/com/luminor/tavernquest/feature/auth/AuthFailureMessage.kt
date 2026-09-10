package com.luminor.tavernquest.feature.auth

import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.functions.FirebaseFunctionsException

internal enum class LoginFailureStage { GOOGLE_CREDENTIAL, FIREBASE_AUTH, ACCOUNT_SETUP }

/** Maps technical failures to actionable copy without exposing identity tokens or server details. */
internal fun loginFailureMessage(error: Throwable, stage: LoginFailureStage): String {
    val detail = error.message.orEmpty()
    return when {
        error is GetCredentialCancellationException || detail.contains("cancel", ignoreCase = true) -> "A entrada com Google foi cancelada."
        error is FirebaseNetworkException || detail.contains("network", ignoreCase = true) || detail.contains("unavailable", ignoreCase = true) -> "Não foi possível conectar agora. Verifique sua internet e tente novamente."
        stage == LoginFailureStage.GOOGLE_CREDENTIAL -> "Não foi possível abrir o seletor de contas Google. Atualize o Google Play Services e tente novamente."
        error is FirebaseAuthInvalidCredentialsException || detail.contains("DEVELOPER_ERROR", ignoreCase = true) || detail.contains("invalid credential", ignoreCase = true) -> "Este APK não foi reconhecido pelo login Google. Instale a versão mais recente do TavernQuest."
        stage == LoginFailureStage.ACCOUNT_SETUP && error is FirebaseFunctionsException && error.code == FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Sua sessão Google expirou. Entre novamente para concluir a criação da conta."
        stage == LoginFailureStage.ACCOUNT_SETUP && error is FirebaseFunctionsException && error.code == FirebaseFunctionsException.Code.NOT_FOUND -> "O serviço de criação de conta está indisponível. Tente novamente em alguns minutos."
        stage == LoginFailureStage.ACCOUNT_SETUP -> "A conta Google foi reconhecida, mas não foi possível preparar seu perfil. Tente novamente."
        else -> "Não foi possível entrar com Google. Tente novamente."
    }
}
