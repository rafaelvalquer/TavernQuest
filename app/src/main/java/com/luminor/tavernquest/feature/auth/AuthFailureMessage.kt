package com.luminor.tavernquest.feature.auth

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.functions.FirebaseFunctionsException

internal enum class LoginFailureStage { GOOGLE_CREDENTIAL, FIREBASE_AUTH, ACCOUNT_SETUP }

/** Maps technical failures to actionable copy without exposing identity tokens or server details. */
internal fun loginFailureMessage(error: Throwable, stage: LoginFailureStage): String {
    val detail = error.message.orEmpty()
    val authCode = (error as? FirebaseAuthException)?.errorCode
    return when {
        error is NoCredentialException -> "Nenhuma conta Google disponível para entrar. Adicione uma conta Google nas configurações do celular e tente novamente."
        error is GetCredentialCancellationException || detail.contains("cancel", ignoreCase = true) -> "A entrada com Google foi cancelada."
        error is FirebaseNetworkException || detail.contains("network", ignoreCase = true) || detail.contains("unavailable", ignoreCase = true) -> "Não foi possível conectar agora. Verifique sua internet e tente novamente."
        authCode == "ERROR_OPERATION_NOT_ALLOWED" -> "O login Google não está habilitado neste ambiente Firebase. Contate o suporte."
        authCode == "ERROR_APP_NOT_AUTHORIZED" || authCode == "ERROR_INVALID_API_KEY" -> "A configuração Firebase deste APK não está autorizada. Instale o APK de produção atualizado."
        authCode == "ERROR_USER_DISABLED" -> "Esta conta foi desativada. Contate o suporte."
        authCode == "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas de entrada. Aguarde alguns minutos e tente novamente."
        stage == LoginFailureStage.GOOGLE_CREDENTIAL -> "Não foi possível abrir o seletor de contas Google. Atualize o Google Play Services e tente novamente."
        error is FirebaseAuthInvalidCredentialsException || detail.contains("DEVELOPER_ERROR", ignoreCase = true) || detail.contains("invalid credential", ignoreCase = true) -> "Este APK não foi reconhecido pelo login Google. Instale a versão mais recente do TavernQuest."
        stage == LoginFailureStage.ACCOUNT_SETUP && error is FirebaseFunctionsException && error.code == FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Sua sessão Google expirou. Entre novamente para concluir a criação da conta."
        stage == LoginFailureStage.ACCOUNT_SETUP && error is FirebaseFunctionsException && error.code == FirebaseFunctionsException.Code.NOT_FOUND -> "O serviço de criação de conta está indisponível. Tente novamente em alguns minutos."
        stage == LoginFailureStage.ACCOUNT_SETUP -> "A conta Google foi reconhecida, mas não foi possível preparar seu perfil. Tente novamente."
        else -> "Não foi possível entrar com Google. Tente novamente. Referência: ${stage.name}/${authCode ?: error.javaClass.simpleName}."
    }
}

