package com.luminor.tavernquest.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthenticated: (Boolean) -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.authenticated) { if (state.authenticated) onAuthenticated(state.needsHero) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Entre para começar sua aventura")
        Text("Use sua conta Google para guardar seu herói, missões e Tabernas com segurança.")
        state.error?.let { Text(it) }
        Button(enabled = !state.loading, modifier = Modifier.fillMaxWidth(), onClick = {
            val clientId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (clientId == 0) return@Button vm.googleUnavailable()
            scope.launch {
                try {
                    val option = GetGoogleIdOption.Builder().setServerClientId(context.getString(clientId))
                        .setFilterByAuthorizedAccounts(false).setAutoSelectEnabled(false).build()
                    val credential = CredentialManager.create(context).getCredential(context, GetCredentialRequest.Builder().addCredentialOption(option).build()).credential
                    if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) return@launch vm.googleUnavailable()
                    vm.googleToken(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                } catch (_: GetCredentialException) { vm.googleUnavailable("Não foi possível concluir o login com Google.") }
            }
        }) { Text(if (state.loading) "Entrando…" else "Continuar com Google") }
    }
}
