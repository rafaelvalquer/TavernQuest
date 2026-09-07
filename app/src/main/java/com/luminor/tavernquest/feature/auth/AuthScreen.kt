package com.luminor.tavernquest.feature.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun AuthScreen(register: Boolean, onAuthenticated: (Boolean) -> Unit, onBack: () -> Unit, onRegister: () -> Unit = {}, vm: AuthViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        runCatching { GoogleSignIn.getSignedInAccountFromIntent(result.data).result?.idToken }.getOrNull()?.let(vm::googleToken) ?: vm.googleUnavailable()
    }
    LaunchedEffect(state.authenticated) { if (state.authenticated) onAuthenticated(state.needsHero) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (register) "Criar conta" else "Entrar na conta")
        if (register) OutlinedTextField(state.displayName, vm::displayName, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.email, vm::email, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.password, vm::password, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it) }
        Button(onClick = { vm.submit(register) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text(if (register) "Criar conta" else "Entrar") }
        Button(onClick = {
            val resource = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resource == 0) vm.googleUnavailable() else {
                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(context.getString(resource)).requestEmail().build()
                googleLauncher.launch(GoogleSignIn.getClient(context, options).signInIntent)
            }
        }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Continuar com Google") }
        if (!register) TextButton(onClick = onRegister, enabled = !state.loading) { Text("Ainda não tenho conta") }
        Button(onClick = onBack, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}
